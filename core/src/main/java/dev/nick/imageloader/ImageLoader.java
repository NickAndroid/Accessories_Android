/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.control.Forkable;
import dev.nick.imageloader.control.Freezer;
import dev.nick.imageloader.control.LoaderState;
import dev.nick.imageloader.control.StorageStats;
import dev.nick.imageloader.control.TrafficStats;
import dev.nick.imageloader.display.BitmapImageSettings;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.ImageQuality;
import dev.nick.imageloader.display.ImageSettable;
import dev.nick.imageloader.display.ImageSettableIdCreator;
import dev.nick.imageloader.display.ImageSettableIdCreatorImpl;
import dev.nick.imageloader.display.ImageViewDelegate;
import dev.nick.imageloader.display.ResImageSettings;
import dev.nick.imageloader.display.animator.ImageAnimator;
import dev.nick.imageloader.display.handler.BitmapHandler;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.imageloader.loader.task.DisplayTask;
import dev.nick.imageloader.loader.task.DisplayTaskImpl;
import dev.nick.imageloader.loader.task.DisplayTaskMonitor;
import dev.nick.imageloader.loader.task.DisplayTaskRecord;
import dev.nick.imageloader.loader.task.FutureImageTask;
import dev.nick.imageloader.loader.task.TaskManager;
import dev.nick.imageloader.loader.task.TaskManagerImpl;
import dev.nick.imageloader.logger.Logger;
import dev.nick.imageloader.logger.LoggerManager;
import dev.nick.imageloader.queue.FIFOPriorityBlockingQueue;
import dev.nick.imageloader.queue.IdleStateMonitor;
import dev.nick.imageloader.queue.LIFOPriorityBlockingQueue;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.queue.QueuePolicy;
import dev.nick.imageloader.queue.RequestHandler;
import dev.nick.imageloader.queue.RequestQueueManager;
import dev.nick.imageloader.utils.Preconditions;

/**
 * Main class of {@link ImageLoader} library.
 */
public class ImageLoader implements DisplayTaskMonitor,
        Handler.Callback,
        RequestHandler<FutureImageTask>,
        FutureImageTask.TaskActionListener,
        Forkable<ImageLoader, LoaderConfig> {

    private static final int MSG_APPLY_IMAGE_SETTINGS = 0x1;
    private static final int MSG_CALL_ON_START = 0x2;
    private static final int MSG_CALL_PROGRESS_UPDATE = 0x3;
    private static final int MSG_CALL_ON_COMPLETE = 0x4;
    private static final int MSG_CALL_ON_FAILURE = 0x5;
    private static final int MSG_CALL_ON_CANCEL = 0x6;

    private static final DisplayOption sDefDisplayOption = DisplayOption.builder()
            .imageQuality(ImageQuality.OPT)
            .imageAnimator(null)
            .bitmapHandler(null)
            .showWithDefault(0)
            .showOnLoading(0)
            .viewMaybeReused()
            .build();

    private static ImageLoader sLoader;

    private final Map<Long, DisplayTaskRecord> mTaskLockMap;
    private final List<FutureImageTask> mFutures;

    private Context mContext;
    private Handler mUIThreadHandler;

    @VisibleForTesting
    private CacheManager mCacheManager;
    @VisibleForTesting
    private LoaderConfig mConfig;
    @VisibleForTesting
    private RequestQueueManager<FutureImageTask> mQueueService;

    private Logger mLogger;

    private long mClearTaskRequestedTimeMills;

    @VisibleForTesting
    private ThreadPoolExecutor mLoadingService, mFallbackService;
    @VisibleForTesting
    private ExecutorService mImageSettingsScheduler;

    private Freezer mFreezer;
    private LoaderState mState;

    private TaskManager mTaskManager;
    private ImageSettableIdCreator mSettableIdCreator;

    private ImageLoader(Context context, CacheManager cacheManager, LoaderConfig config) {
        Preconditions.checkNotNull(context);
        if (config == null) config = LoaderConfig.DEFAULT_CONFIG;
        LoggerManager.setDebugLevel(config.getDebugLevel());
        this.mContext = context;
        this.mConfig = config;
        this.mTaskManager = new TaskManagerImpl();
        this.mSettableIdCreator = new ImageSettableIdCreatorImpl();
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        this.mCacheManager = cacheManager == null
                ? new CacheManager(config.getCachePolicy(), context)
                : cacheManager;
        this.mLoadingService = new ThreadPoolExecutor(
                config.getLoadingThreads(),
                config.getLoadingThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                config.getQueuePolicy() == QueuePolicy.FIFO
                        ? new FIFOPriorityBlockingQueue<Runnable>()
                        : new LIFOPriorityBlockingQueue<Runnable>());
        this.mImageSettingsScheduler = Executors.newSingleThreadExecutor();
        this.mQueueService = RequestQueueManager.createStarted(this, new IdleStateMonitor() {
            @Override
            public void onIdle() {
                LoggerManager.getLogger(IdleStateMonitor.class).funcEnter();
                TrafficStats.from(mContext).flush();
                StorageStats.from(mContext).flush();
            }
        }, QueuePolicy.FIFO);
        this.mTaskLockMap = new HashMap<>();
        this.mFutures = new ArrayList<>();
        this.mState = LoaderState.RUNNING;
        this.mLogger = LoggerManager.getLogger(getClass().getSimpleName()
                + "#"
                + LoaderFactory.assignLoaderId());
        this.mLogger.info("Create loader with config " + config);
    }

    private static ImageLoader clone(ImageLoader from, LoaderConfig config) {
        return new ImageLoader(from.mContext, from.mCacheManager, config);
    }

    /**
     * Create the shared instance of ImageLoader
     *
     * @param context An application {@link Context} is preferred.
     * @since 1.0.1
     */
    public static void createShared(Context context) {
        createShared(context, null);
    }

    /**
     * Create the shared instance of ImageLoader
     *
     * @param context An application {@link Context} is preferred.
     * @param config  Configuration of this loader.
     * @since 1.0.1
     */
    public static void createShared(Context context, LoaderConfig config) {
        if (sLoader == null || sLoader.isTerminated()) {
            sLoader = new ImageLoader(context, null, config);
        }
    }

    /**
     * Get the createShared instance of ImageLoader
     *
     * @return Single instance of {@link ImageLoader}
     * @since 1.0.1
     */
    public static ImageLoader shared() {
        return Preconditions.checkNotNull(sLoader, "Call createShared first");
    }

    /**
     * @return An optional params wrapper.
     * @see DisplayOptional
     */
    public DisplayOptional display() {
        return new DisplayOptional(this);
    }

    /**
     * @return An optional params wrapper.
     * @see LoadOptional
     */
    public LoadOptional load() {
        return new LoadOptional(this);
    }

    /**
     * Load image from url.
     *
     * @param url             The url to load.
     * @param loadingListener Listener to listen the progress of the loading process.
     * @param priority        Priority for this loading session.
     * @see Priority
     * @see LoadingListener
     */
    public void load(@NonNull String url, @NonNull LoadingListener loadingListener, Priority priority) {
        display(url, new FakeImageSettable(url),
                DisplayOption.builder()
                        .imageQuality(ImageQuality.OPT)
                        .imageAnimator(null)
                        .bitmapHandler(null)
                        .viewMaybeReused()
                        .showWithDefault(0)
                        .showOnLoading(0)
                        .build(),
                Preconditions.checkNotNull(loadingListener),
                priority);
    }

    public void display(@NonNull String url,
                        @NonNull ImageView view,
                        @Nullable DisplayOption option,
                        @Nullable DisplayListener listener,
                        @Nullable Priority priority) {
        display(url, new ImageViewDelegate(view), option, listener, priority);
    }

    /**
     * Display image from the url to the view.
     *
     * @param url      Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param settable Target {@link ImageSettable} to display the image.
     * @param option   {@link DisplayOption} is options using when display the image.
     * @param listener The progress listener using to watch the progress of the loading.
     */
    public void display(@NonNull String url,
                        @NonNull ImageSettable settable,
                        @Nullable DisplayOption option,
                        @Nullable DisplayListener listener,
                        @Nullable Priority priority) {

        ensureNotTerminated();

        Preconditions.checkNotNull(url, settable);

        DisplayTaskRecord record = createTaskRecord(settable);

        option = assignOptionIfNull(option);

        beforeLoading(settable, option);

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        ViewSpec info = new ViewSpec(settable.getWidth(), settable.getHeight());

        if (mCacheManager.isMemCacheEnabled()) {
            Bitmap cached;
            if ((cached = mCacheManager.getMemCache(url, info)) != null) {
                mLogger.verbose("MemCache, Load cached mem bitmap:" + cached);

                applyImageSettings(
                        cached,
                        option.getProcessor(),
                        settable,
                        option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                // Call complete.
                if (listener != null) {
                    BitmapResult result = new BitmapResult();
                    result.result = cached;
                    listener.onComplete(result);
                }
                return;
            }
        }

        String loadingUrl = url;

        if (mCacheManager.isDiskCacheEnabled()) {
            String cachePath;
            if ((cachePath = mCacheManager.getDiskCachePath(url, info)) != null) {
                mLogger.verbose("DiskCache, Load cached disk cache:" + cachePath);
                loadingUrl = ImageSource.FILE.getPrefix() + cachePath;
            }
        }

        loadAndDisplay(loadingUrl, settable, option, listener, record, priority);
    }

    private DisplayTaskRecord createTaskRecord(ImageSettable settable) {
        long settableId = mSettableIdCreator.createSettableId(settable);
        int taskId = mTaskManager.nextTaskId();
        DisplayTaskRecord displayTaskRecord = new DisplayTaskRecord(settableId, taskId);
        onTaskCreated(displayTaskRecord);
        return displayTaskRecord;
    }

    private void beforeLoading(ImageSettable settable, DisplayOption option) {
        int showWhenLoading = option.getLoadingImgRes();
        applyImageSettings(showWhenLoading, settable, null);
    }

    private void loadAndDisplay(String url,
                                ImageSettable settable,
                                DisplayOption option,
                                DisplayListener listener,
                                DisplayTaskRecord record,
                                Priority priority) {

        ImageQuality imageQuality = option.getQuality();

        ViewSpec viewSpec = new ViewSpec(settable.getWidth(), settable.getHeight());

        ProgressListenerDelegate progressListenerDelegate = new ProgressListenerDelegate(
                listener,
                viewSpec,
                option,
                settable,
                record,
                url);

        ErrorListenerDelegate errorListenerDelegate = null;

        if (listener != null) {
            errorListenerDelegate = new ErrorListenerDelegate(listener);
        }

        DisplayTaskImpl imageTask = new DisplayTaskImpl(
                mContext,
                mConfig,
                this,
                url,
                viewSpec,
                imageQuality,
                progressListenerDelegate,
                errorListenerDelegate,
                record);

        FutureImageTask future = new FutureImageTask(imageTask, this, option.isViewMaybeReused());
        future.setPriority(priority == null ? Priority.NORMAL : priority);

        // Push it to the request queue.
        mQueueService.push(future);
    }

    private DisplayOption assignOptionIfNull(DisplayOption option) {
        if (option != null) return option;
        return sDefDisplayOption;
    }

    private void onTaskCreated(DisplayTaskRecord record) {
        mLogger.verbose("Created task:" + record);
        int taskId = record.getTaskId();
        long settableId = record.getSettableId();
        synchronized (mTaskLockMap) {
            DisplayTaskRecord exists = mTaskLockMap.get(settableId);
            if (exists != null) {
                exists.setTaskId(taskId);
            } else {
                mTaskLockMap.put(settableId, record);
            }
        }
    }

    private boolean onFutureSubmit(FutureImageTask futureImageTask) {
        if (futureImageTask.shouldCancelOthersBeforeRun()) {
            cancel(futureImageTask.getListenableTask().getTaskRecord().getSettableId());
        }
        synchronized (mFutures) {
            mFutures.add(futureImageTask);
        }
        return true;
    }

    private void onFutureDone(FutureImageTask futureImageTask) {
        synchronized (mFutures) {
            mFutures.remove(futureImageTask);
        }
    }

    private void onFutureCancel(FutureImageTask futureImageTask) {
        synchronized (mFutures) {
            mFutures.remove(futureImageTask);
        }
    }

    private boolean isTaskDirty(DisplayTaskRecord task) {

        boolean outDated = task.upTime() <= mClearTaskRequestedTimeMills;

        if (outDated) {
            return true;
        }

        synchronized (mTaskLockMap) {
            DisplayTaskRecord lock = mTaskLockMap.get(task.getSettableId());
            if (lock != null) {
                int taskId = lock.getTaskId();
                // We have new task to load for this settle.
                if (taskId > task.getTaskId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onApplyImageSettings(Runnable settings) {
        settings.run();
    }

    @WorkerThread
    private void applyImageSettings(Bitmap bitmap, BitmapHandler processor, ImageSettable settable,
                                    ImageAnimator animator) {
        if (settable != null) {
            BitmapImageSettings settings = new BitmapImageSettings(mContext.getResources(), animator,
                    (processor == null ? bitmap : processor.process(bitmap, settable)), settable);
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    @WorkerThread
    private void applyImageSettings(int resId, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            ResImageSettings settings = new ResImageSettings(animator, resId, settable);
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_APPLY_IMAGE_SETTINGS:
                onApplyImageSettings((Runnable) message.obj);
                break;
            case MSG_CALL_ON_START:
                onCallOnStart((ProgressListener<BitmapResult>) message.obj);
                break;
            case MSG_CALL_ON_COMPLETE:
                onCallOnComplete((CompleteParams) message.obj);
                break;
            case MSG_CALL_ON_FAILURE:
                onCallOnFailure((FailureParams) message.obj);
                break;
            case MSG_CALL_ON_CANCEL:
                onCallOnCancel((ProgressListener<BitmapResult>) message.obj);
                break;
            case MSG_CALL_PROGRESS_UPDATE:
                onCallOnProgressUpdate((ProgressParams) message.obj);
                break;
        }
        return true;
    }

    private void callOnStart(ProgressListener listener) {
        if (listener != null) {
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_START, listener).sendToTarget();
        }
    }

    private void callOnCancel(ProgressListener listener) {
        if (listener != null) {
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_CANCEL, listener).sendToTarget();
        }
    }

    private void callOnProgressUpdate(ProgressListener<BitmapResult> listener, float progress) {
        if (listener != null) {
            ProgressParams progressParams = new ProgressParams();
            progressParams.progress = progress;
            progressParams.progressListener = listener;
            mUIThreadHandler.obtainMessage(MSG_CALL_PROGRESS_UPDATE, progressParams).sendToTarget();
        }
    }

    private void callOnComplete(ProgressListener<BitmapResult> listener, BitmapResult result) {
        if (listener != null) {
            CompleteParams completeParams = new CompleteParams();
            completeParams.progressListener = listener;
            completeParams.result = result;
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_COMPLETE, completeParams).sendToTarget();
        }
    }

    private void callOnFailure(ErrorListener listener, Cause cause) {
        if (listener != null) {
            FailureParams failureParams = new FailureParams();
            failureParams.cause = cause;
            failureParams.listener = listener;
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_FAILURE, failureParams).sendToTarget();
        }
    }

    private void onCallOnStart(ProgressListener<BitmapResult> listener) {
        listener.onStartLoading();
    }

    private void onCallOnCancel(ProgressListener<BitmapResult> listener) {
        listener.onCancel();
    }

    private void onCallOnProgressUpdate(ProgressParams progressParams) {
        progressParams.progressListener.onProgressUpdate(progressParams.progress);
    }

    private void onCallOnComplete(CompleteParams params) {
        params.progressListener.onComplete(params.result);
    }

    private void onCallOnFailure(FailureParams params) {
        params.listener.onError(params.cause);
    }

    @Override
    public boolean handleRequest(FutureImageTask future) {
        freezeIfRequested();
        if (!onFutureSubmit(future)) return false;
        getExecutor(ImageSource.of(future.getListenableTask().getUrl())).submit(future);
        return true;
    }

    private ExecutorService getExecutor(ImageSource source) {

        switch (source) {
            case NETWORK_HTTP: // fall.
            case NETWORK_HTTPS:
                return mLoadingService;
            default:
                int activeThreads = mLoadingService.getActiveCount();
                int max = mLoadingService.getMaximumPoolSize();
                if (activeThreads == max) {
                    mLogger.verbose("The loading service hits, using fallback one.");
                    ensureFallbackService();
                    return mFallbackService;
                }
                break;
        }
        return mLoadingService;
    }

    private synchronized void ensureFallbackService() {
        if (mFallbackService == null) {
            int poolSize = mConfig.getLoadingThreads();
            poolSize = poolSize / 2 + 1;
            this.mFallbackService = new ThreadPoolExecutor(
                    poolSize,
                    poolSize,
                    0L,
                    TimeUnit.MILLISECONDS,
                    mConfig.getQueuePolicy() == QueuePolicy.FIFO
                            ? new FIFOPriorityBlockingQueue<Runnable>()
                            : new LIFOPriorityBlockingQueue<Runnable>());
            mLogger.verbose("Created fallback service with pool size:" + poolSize);
        }
    }

    /**
     * @return {@link CacheManager} instance of this loader is using.
     */
    public CacheManager getCacheManager() {
        return mCacheManager;
    }

    /**
     * Call this to pause the {@link ImageLoader}
     */
    public void pause() {
        ensureNotTerminated();
        if (!isPaused()) {
            mState = LoaderState.PAUSE_REQUESTED;
        }
        mLogger.funcExit();
    }

    /**
     * @return {@code true} if this loader is paused.
     * @see #pause()
     */
    public boolean isPaused() {
        return mState == LoaderState.PAUSED || mState == LoaderState.PAUSE_REQUESTED;
    }

    /**
     * @return {@code true} if this loader is terminated.
     * @see #terminate() ()
     */
    public boolean isTerminated() {
        return mState == LoaderState.TERMINATED;
    }

    /**
     * Call this to resume the {@link ImageLoader} from pause state.
     *
     * @see #pause()
     * @see LoaderState
     */
    public void resume() {
        ensureNotTerminated();
        if (isPaused()) {
            if (mFreezer != null) {
                mFreezer.resume();
            }
            mState = LoaderState.RUNNING;
            mLogger.funcExit();
        }
    }

    /**
     * Clear all pending tasks.
     */
    public void clearTasks() {
        ensureNotTerminated();
        mClearTaskRequestedTimeMills = System.currentTimeMillis();
    }

    /**
     * Terminate the loader.
     */
    public void terminate() {
        ensureNotTerminated();
        mState = LoaderState.TERMINATED;
        mQueueService.terminate();
        mLoadingService.shutdown();
        synchronized (mTaskLockMap) {
            mTaskLockMap.clear();
        }
        cancelAllTasks();
        mLogger.funcExit();
    }

    private void ensureNotTerminated() {
        Preconditions.checkState(!isTerminated(), "Loader has already been terminated");
    }

    /**
     * Clear all the load and loading tasks.
     */
    public void cancelAllTasks() {
        synchronized (mFutures) {
            for (FutureImageTask futureImageTask : mFutures) {
                futureImageTask.cancel(true);
            }
            mFutures.clear();
        }
    }

    /**
     * Cancel the load and loading task who's url match given.
     *
     * @param url The url of the loader request.
     */
    public ImageLoader cancel(@NonNull String url) {
        Preconditions.checkNotNull(url);
        List<FutureImageTask> pendingCancels = findTasks(url);
        if (pendingCancels.size() > 0) {
            for (FutureImageTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                callOnCancel(toBeCanceled.getListenableTask().getProgressListener());
                mLogger.info("Cancel task for url:" + url);
            }
            pendingCancels.clear();
            pendingCancels = null;
        }
        return this;
    }

    /**
     * Cancel the load and loading task who's view match given.
     *
     * @param view The view of the loader request.
     */
    public ImageLoader cancel(@NonNull ImageView view) {
        return cancel(new ImageViewDelegate(view));
    }

    /**
     * Cancel the load and loading task who's settable match given.
     *
     * @param settable The settable of the loader request.
     */
    public ImageLoader cancel(@NonNull ImageSettable settable) {
        return cancel(mSettableIdCreator.createSettableId(settable));
    }

    /**
     * Cancel the load and loading task who's settableId match given.
     *
     * @param settableId The settableId of the loader request.
     */
    public ImageLoader cancel(long settableId) {
        Preconditions.checkState(settableId != 0, "Invalid settable with id:0");
        List<FutureImageTask> pendingCancels = findTasks(settableId);
        if (pendingCancels.size() > 0) {
            for (FutureImageTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                callOnCancel(toBeCanceled.getListenableTask().getProgressListener());
                mLogger.verbose("Cancel task for settable:" + settableId);
            }
            pendingCancels.clear();
            pendingCancels = null;
        }
        return this;
    }

    private List<FutureImageTask> findTasks(@NonNull String url) {
        List<FutureImageTask> pendingCancels = new ArrayList<>();
        synchronized (mFutures) {
            for (FutureImageTask futureImageTask : mFutures) {
                if (!futureImageTask.isCancelled()
                        && !futureImageTask.isDone()
                        && url.equals(futureImageTask.getListenableTask().getUrl())) {
                    pendingCancels.add(futureImageTask);
                }
            }
        }
        return pendingCancels;
    }

    private List<FutureImageTask> findTasks(@NonNull ImageView view) {
        return findTasks(new ImageViewDelegate(view));
    }

    private List<FutureImageTask> findTasks(@NonNull ImageSettable settable) {
        return findTasks(mSettableIdCreator.createSettableId(settable));
    }

    private List<FutureImageTask> findTasks(long settableId) {
        List<FutureImageTask> pendingCancels = new ArrayList<>();
        synchronized (mFutures) {
            for (FutureImageTask futureImageTask : mFutures) {
                if (!futureImageTask.isCancelled()
                        && !futureImageTask.isDone()
                        && settableId == futureImageTask.getListenableTask().getTaskRecord().getSettableId()) {
                    pendingCancels.add(futureImageTask);
                }
            }
        }
        return pendingCancels;
    }

    @WorkerThread
    public void clearAllCache() {
        clearDiskCache();
        clearMemCache();
        mLogger.funcExit();
    }

    @WorkerThread
    public void clearDiskCache() {
        mCacheManager.evictDisk();
        StorageStats.from(mContext).reset();
        mLogger.funcExit();
    }

    @WorkerThread
    public void clearMemCache() {
        mCacheManager.evictMem();
        mLogger.funcExit();
    }

    @Override
    public boolean shouldRun(@NonNull DisplayTask task) {
        if (!checkInRunningState()) return false;
        // Check if this task is dirty.
        boolean isTaskDirty = isTaskDirty(task.getTaskRecord());
        if (isTaskDirty) {
            mLogger.info("Won't run task:" + task);
            return false;
        }
        return true;
    }

    boolean checkInRunningState() {
        return mState != LoaderState.TERMINATED;
    }

    void freezeIfRequested() {
        if (mState == LoaderState.PAUSE_REQUESTED) {
            mState = LoaderState.PAUSED;
            if (mFreezer == null) mFreezer = new Freezer();
            mLogger.debug("Freezing the loader...");
            mFreezer.freeze();
        }
    }

    @Override
    public void onDone(FutureImageTask futureImageTask) {
        mLogger.verbose(futureImageTask.getListenableTask().getTaskRecord());
        onFutureDone(futureImageTask);
    }

    @Override
    public void onCancel(FutureImageTask futureImageTask) {
        mLogger.verbose(futureImageTask.getListenableTask().getTaskRecord());
        onFutureCancel(futureImageTask);
    }

    @Override
    public ImageLoader fork(LoaderConfig config) {
        return clone(this, config);
    }

    public long getInternalStorageUsage() {
        return StorageStats.from(mContext).getInternalStorageUsage();
    }

    public long getExternalStorageUsage() {
        return StorageStats.from(mContext).getExternalStorageUsage();
    }

    public long getTotalStorageUsage() {
        return StorageStats.from(mContext).getTotalStorageUsage();
    }

    public long getTotalTrafficUsage() {
        return TrafficStats.from(mContext).getTotalTrafficUsage();
    }

    public long getMobileTrafficUsage() {
        return TrafficStats.from(mContext).getMobileTrafficUsage();
    }

    public long getWifiTrafficUsage() {
        return TrafficStats.from(mContext).getWifiTrafficUsage();
    }

    private static class FailureParams {
        Cause cause;
        ErrorListener listener;
    }

    private static class CompleteParams {
        BitmapResult result;
        ProgressListener<BitmapResult> progressListener;
    }

    private static class ProgressParams {
        float progress;
        ProgressListener<BitmapResult> progressListener;
    }

    private static class LoaderFactory {
        private static AtomicInteger sLoaderId = new AtomicInteger(0);

        static int assignLoaderId() {
            return sLoaderId.getAndIncrement();
        }
    }

    public static class DisplayOptional {

        private String url;
        private DisplayOption option;
        private DisplayListener listener;
        private Priority priority;

        private ImageLoader loader;

        private DisplayOptional(@NonNull ImageLoader loader) {
            this.loader = loader;
        }

        public DisplayOptional url(@NonNull String url) {
            this.url = Preconditions.checkNotNull(url);
            return DisplayOptional.this;
        }

        public DisplayOptional option(@NonNull DisplayOption option) {
            this.option = Preconditions.checkNotNull(option);
            return DisplayOptional.this;
        }

        public DisplayOptional listener(@NonNull DisplayListener listener) {
            this.listener = Preconditions.checkNotNull(listener);
            return DisplayOptional.this;
        }

        public DisplayOptional priority(@NonNull Priority priority) {
            this.priority = Preconditions.checkNotNull(priority);
            return DisplayOptional.this;
        }

        public void into(@NonNull ImageSettable settable) {
            loader.display(url, Preconditions.checkNotNull(settable), option, listener, priority);
        }

        public void into(@NonNull ImageView view) {
            ImageViewDelegate viewDelegate = new ImageViewDelegate(view);
            loader.display(url, viewDelegate, option, listener, priority);
        }
    }

    public static class LoadOptional {

        private String url;

        private Priority priority;
        private LoadingListener listener;

        private ImageLoader loader;

        private LoadOptional(@NonNull ImageLoader loader) {
            this.loader = loader;
        }

        public LoadOptional url(@NonNull String url) {
            this.url = Preconditions.checkNotNull(url);
            return LoadOptional.this;
        }

        public LoadOptional listener(@NonNull LoadingListener listener) {
            this.listener = Preconditions.checkNotNull(listener);
            return LoadOptional.this;
        }

        public LoadOptional priority(@NonNull Priority priority) {
            this.priority = Preconditions.checkNotNull(priority);
            return LoadOptional.this;
        }

        public void start() {
            loader.load(url, listener, priority);
        }
    }

    class FakeImageSettable implements ImageSettable {

        String url;

        public FakeImageSettable(String url) {
            this.url = url;
        }

        @Override
        public void setImageBitmap(@NonNull Bitmap bitmap) {
            // Nothing.
        }

        @Override
        public void setImageResource(int resId) {
            // Nothing.
        }

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public void startAnimation(Animation animation) {
            // Nothing.
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }
    }

    class ImageSettingsLocker {

        private final static long MAX_DELAY = 2 * 1000;

        private CountDownLatch latch;

        private long unLockDelay;

        public ImageSettingsLocker(long unLockDelay) {
            this.unLockDelay = unLockDelay;
            this.latch = new CountDownLatch(1);
        }

        void lock() {
            while (true) {
                try {
                    latch.await(unLockDelay > MAX_DELAY ? MAX_DELAY : unLockDelay, TimeUnit.MILLISECONDS);
                    break;
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    private class ProgressListenerDelegate implements ProgressListener<BitmapResult> {

        private ProgressListener<BitmapResult> listener;

        @NonNull
        private ImageSettable settable;
        private String url;
        private DisplayOption option;
        private ViewSpec viewSpec;

        private DisplayTaskRecord taskRecord;

        private Boolean canceled = Boolean.FALSE;
        private Boolean isTaskDirty = null;

        public ProgressListenerDelegate(ProgressListener<BitmapResult> listener,
                                        ViewSpec viewSpec,
                                        DisplayOption option,
                                        @NonNull ImageSettable settable,
                                        DisplayTaskRecord taskRecord,
                                        String url) {
            this.viewSpec = viewSpec;
            this.listener = listener;
            this.option = option;
            this.settable = settable;
            this.taskRecord = taskRecord;
            this.url = url;
        }

        @Override
        public void onStartLoading() {
            if (!canceled && !checkTaskDirty()) {
                callOnStart(listener);
            }
        }

        @Override
        public void onProgressUpdate(float progress) {
            if (!canceled && !checkTaskDirty()) {
                callOnProgressUpdate(listener, progress);
            }
        }

        @Override
        public void onCancel() {
            canceled = Boolean.TRUE;
            if (!checkTaskDirty()) {
                callOnCancel(listener);
            }
        }

        @Override
        public void onComplete(final BitmapResult result) {

            if (result.result == null) {
                return;
            }

            if (canceled) {
                mCacheManager.cache(url, viewSpec, result.result);
                return;
            }

            callOnComplete(listener, result);

            final boolean isViewMaybeReused = option.isViewMaybeReused();

            if (!isViewMaybeReused || !isTaskDirty(taskRecord)) {
                if (!option.isApplyImageOneByOne()) {
                    ImageAnimator animator = (option == null ? null : option.getAnimator());
                    BitmapHandler processor = (option == null ? null : option.getProcessor());
                    applyImageSettings(result.result, processor, settable, animator);
                } else {
                    mImageSettingsScheduler.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isViewMaybeReused && isTaskDirty(taskRecord)) return;
                            ImageAnimator animator = (option == null ? null : option.getAnimator());
                            BitmapHandler processor = (option == null ? null : option.getProcessor());
                            applyImageSettings(result.result, processor, settable, animator);
                            if (animator != null) {
                                long delay = animator.getDuration();
                                ImageSettingsLocker locker = new ImageSettingsLocker(delay / 5);
                                locker.lock();
                            }
                        }
                    });
                }
            } else {
                mLogger.info("Won't apply image settings for task:" + taskRecord);
            }
            mCacheManager.cache(url, viewSpec, result.result);
        }

        synchronized boolean checkTaskDirty() {
            if (isTaskDirty == null || !isTaskDirty) {
                isTaskDirty = isTaskDirty(taskRecord);
            }
            return isTaskDirty;
        }
    }

    private class ErrorListenerDelegate implements ErrorListener {

        ErrorListener listener;

        public ErrorListenerDelegate(ErrorListener listener) {
            this.listener = listener;
        }

        @Override
        public void onError(@NonNull Cause cause) {
            if (cause.exception instanceof InterruptedIOException) {
                // We canceled this task.
            } else {
                callOnFailure(listener, cause);
            }
        }
    }
}
