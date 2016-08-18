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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.control.Forkable;
import dev.nick.imageloader.control.Freezer;
import dev.nick.imageloader.control.LoaderState;
import dev.nick.imageloader.control.StorageStats;
import dev.nick.imageloader.control.TrafficStats;
import dev.nick.imageloader.debug.Logger;
import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ImageSourceType;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.imageloader.loader.task.DisplayTask;
import dev.nick.imageloader.loader.task.DisplayTaskMonitor;
import dev.nick.imageloader.loader.task.DisplayTaskRecord;
import dev.nick.imageloader.loader.task.FutureImageTask;
import dev.nick.imageloader.loader.task.MokeFutureImageTask;
import dev.nick.imageloader.loader.task.TaskManager;
import dev.nick.imageloader.queue.FIFOPriorityBlockingQueue;
import dev.nick.imageloader.queue.IdleStateMonitor;
import dev.nick.imageloader.queue.LIFOPriorityBlockingQueue;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.queue.QueuePolicy;
import dev.nick.imageloader.queue.RequestHandler;
import dev.nick.imageloader.queue.RequestQueueManager;
import dev.nick.imageloader.ui.BitmapImageSeat;
import dev.nick.imageloader.ui.BitmapImageViewDelegate;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageQuality;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.ImageSettableIdCreator;
import dev.nick.imageloader.ui.ImageSettableIdCreatorImpl;
import dev.nick.imageloader.utils.Preconditions;

/**
 * Main class of {@link ImageLoader} library.
 */
public class ImageLoader implements DisplayTaskMonitor<Bitmap>,
        FutureImageTask.TaskActionListener,
        Forkable<ImageLoader, LoaderConfig>,
        Terminable {

    private static final DisplayOption<Bitmap> sDefDisplayOption = DisplayOption.bitmapBuilder()
            .imageQuality(ImageQuality.OPT)
            .viewMaybeReused()
            .build();

    private static ImageLoader sLoader;

    private final List<FutureImageTask> mFutures;

    private Context mContext;

    private UIThreadRouter mUiThreadRouter;
    private ImageSettingApplier mImageSettingApplier;

    private CacheManager<Bitmap> mCacheManager;
    private LoaderConfig mConfig;
    private RequestQueueManager<FutureImageTask> mTaskHandleService;
    private RequestQueueManager<Transaction> mTransactionService;

    private Logger mLogger;

    private ThreadPoolExecutor mLoadingService, mFallbackService;

    private Freezer mFreezer;
    private LoaderState mState;

    private TaskManager mTaskManager;
    private ImageSettableIdCreator mSettableIdCreator;

    @SuppressLint("DefaultLocale")
    private ImageLoader(Context context, CacheManager<Bitmap> cacheManager, LoaderConfig config) {
        Preconditions.checkNotNull(context);
        if (config == null) config = LoaderConfig.DEFAULT_CONFIG;
        LoggerManager.setDebugLevel(config.getDebugLevel());
        this.mContext = context;
        this.mConfig = config;
        this.mTaskManager = new TaskManagerImpl();
        this.mSettableIdCreator = new ImageSettableIdCreatorImpl();
        this.mUiThreadRouter = UIThreadRouter.getSharedRouter();
        this.mImageSettingApplier = ImageSettingApplier.getSharedApplier();
        this.mCacheManager = cacheManager == null
                ? new BitmapCacheManager(config.getCachePolicy(), context)
                : cacheManager;
        //noinspection deprecation
        this.mLoadingService = new ThreadPoolExecutor(
                config.getLoadingThreads(),
                config.getLoadingThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                config.getQueuePolicy() == QueuePolicy.FIFO
                        ? new FIFOPriorityBlockingQueue<Runnable>()
                        : new LIFOPriorityBlockingQueue<Runnable>());
        int loaderId = LoaderFactory.assignLoaderId();
        this.mTaskHandleService = RequestQueueManager.createStarted(new TaskHandler(), null, null, "TaskHandleService#" + loaderId);
        this.mTransactionService = RequestQueueManager.createStarted(new TransactionHandler(), new IdleStateMonitor() {
            @Override
            public void onIdle() {
                LoggerManager.getLogger(IdleStateMonitor.class).funcEnter();
                TrafficStats.from(mContext).flush();
                StorageStats.from(mContext).flush();
            }
        }, QueuePolicy.FIFO, "TransactionService#" + loaderId);
        this.mFutures = new ArrayList<>();
        this.mState = LoaderState.RUNNING;
        this.mLogger = LoggerManager.getLogger(getClass().getSimpleName() + "#" + loaderId);
        this.mLogger.verbose(String.format("Create loader-%d with config %s", loaderId, config));
    }

    @LoaderApi
    private static ImageLoader clone(ImageLoader from, LoaderConfig config) {
        return new ImageLoader(from.mContext, from.mCacheManager, config);
    }

    /**
     * Create the shared instance of ImageLoader
     *
     * @param context An application {@link Context} is preferred.
     * @since 1.0.1
     */
    @LoaderApi
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
    @LoaderApi
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
    @LoaderApi
    public static ImageLoader shared() {
        return Preconditions.checkNotNull(sLoader, "Call ImageLoader#createShared first");
    }

    /**
     * Start a quick optional transaction builder,
     * do not forget to call {@link Transaction#start()} to start this task.
     *
     * @return An optional params wrapper.
     * @see Transaction
     */
    @LoaderApi
    public BitmapTransaction loadBitmap() {
        return new BitmapTransaction(this);
    }

    /**
     * Display image from the from to the view.
     *
     * @param source           Image source from, one of {@link ImageSource}
     * @param settable         Target {@link BitmapImageSeat} to display the image.
     * @param option           {@link DisplayOption} is options using when display the image.
     * @param progressListener The progress progressListener using to watch the progress of the loading.
     */
    Future<Bitmap> displayBitmap(@NonNull ImageSource<Bitmap> source,
                                 @NonNull ImageSeat<Bitmap> settable,
                                 @Nullable DisplayOption<Bitmap> option,
                                 @Nullable ProgressListener<Bitmap> progressListener,
                                 @Nullable ErrorListener errorListener,
                                 @Nullable Priority priority) {

        ensureNotTerminated();

        Preconditions.checkNotNull(source.getUrl(), "imageSource is null");

        DisplayTaskRecord record = createTaskRecord(Preconditions.checkNotNull(settable));

        option = assignOptionIfNull(option);

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        if (mCacheManager.isMemCacheEnabled()) {
            Bitmap cached;
            if ((cached = mCacheManager.get(source.getUrl())) != null) {
                mImageSettingApplier.applyImageSettings(
                        cached,
                        option.getHandlers(),
                        settable,
                        option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                // Call complete.
                if (progressListener != null) {
                    progressListener.onComplete(cached);
                }
                return new MokeFutureImageTask<>(cached);
            }
        }

        beforeLoading(settable, option);

        String loadingUrl = source.getUrl();

        if (mCacheManager.isDiskCacheEnabled()) {
            String cachePath;
            if ((cachePath = mCacheManager.getCachePath(loadingUrl)) != null) {
                loadingUrl = ImageSourceType.FILE.getPrefix() + cachePath;
                // Check mem cache again.
                if (mCacheManager.isMemCacheEnabled()) {
                    Bitmap cached;
                    if ((cached = mCacheManager.get(loadingUrl)) != null) {
                        mImageSettingApplier.applyImageSettings(
                                cached,
                                option.getHandlers(),
                                settable,
                                option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                        // Call complete.
                        if (progressListener != null) {
                            progressListener.onComplete(cached);
                        }
                        return new MokeFutureImageTask<>(cached);
                    }
                }
                source.setUrl(loadingUrl);
            }
        }

        return loadAndDisplayBitmap(source, settable, option, progressListener, errorListener, record, priority);
    }

    private DisplayTaskRecord createTaskRecord(ImageSeat<Bitmap> settable) {
        long settableId = mSettableIdCreator.createSettableId(settable);
        int taskId = mTaskManager.nextTaskId();
        DisplayTaskRecord displayTaskRecord = new DisplayTaskRecord(settableId, taskId);
        mTaskManager.onDisplayTaskCreated(displayTaskRecord);
        return displayTaskRecord;
    }

    private void beforeLoading(ImageSeat<Bitmap> settable, DisplayOption option) {
        int showWhenLoading = option.getLoadingImgRes();
        mImageSettingApplier.applyImageSettings(showWhenLoading, settable, null);
    }

    private Future<Bitmap> loadAndDisplayBitmap(ImageSource<Bitmap> source,
                                                ImageSeat<Bitmap> imageSeat,
                                                DisplayOption<Bitmap> option,
                                                ProgressListener<Bitmap> progressListener,
                                                ErrorListener errorListener,
                                                DisplayTaskRecord record,
                                                Priority priority) {

        ImageQuality imageQuality = option.getQuality();

        ViewSpec viewSpec = new ViewSpec(imageSeat.getWidth(), imageSeat.getHeight());

        ProgressListenerDelegate<Bitmap> progressListenerDelegate = new BitmapProgressListenerDelegate(
                mCacheManager,
                mTaskManager,
                progressListener,
                viewSpec,
                option,
                imageSeat,
                record,
                source.getUrl());

        ErrorListenerDelegate errorListenerDelegate = null;

        if (progressListener != null) {
            errorListenerDelegate = new ErrorListenerDelegate(errorListener);
        }

        BitmapDisplayTask imageTask = new BitmapDisplayTask(
                mContext,
                mConfig,
                this,
                source,
                viewSpec,
                imageQuality,
                progressListenerDelegate,
                errorListenerDelegate,
                record);

        FutureImageTask future = new FutureImageTask(imageTask, this, option.isViewMaybeReused());
        future.setPriority(priority == null ? Priority.NORMAL : priority);

        mTaskHandleService.push(future);
        return future;
    }

    private DisplayOption<Bitmap> assignOptionIfNull(DisplayOption<Bitmap> option) {
        if (option != null) return option;
        return sDefDisplayOption;
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

    private ExecutorService getExecutor(ImageSourceType type) {
        if (type.isOneOf(ImageSourceType.NETWORK_HTTP, ImageSourceType.NETWORK_HTTPS)) {
            return mLoadingService;
        } else {
            int activeThreads = mLoadingService.getActiveCount();
            int max = mLoadingService.getMaximumPoolSize();
            if (activeThreads == max) {
                mLogger.warn("The loading service hits, using fallback one.");
                ensureFallbackService();
                return mFallbackService;
            }
        }
        return mLoadingService;
    }

    RequestQueueManager<Transaction> getTransactionService() {
        return mTransactionService;
    }

    @SuppressWarnings("deprecation")
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
    @LoaderApi
    public CacheManager getCacheManager() {
        return mCacheManager;
    }

    /**
     * Call this to pause the {@link ImageLoader}
     */
    @LoaderApi
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
    @LoaderApi
    public boolean isPaused() {
        return mState == LoaderState.PAUSED || mState == LoaderState.PAUSE_REQUESTED;
    }

    /**
     * @return {@code true} if this loader is terminated.
     * @see #terminate() ()
     */
    @LoaderApi
    public boolean isTerminated() {
        return mState == LoaderState.TERMINATED;
    }

    /**
     * Call this to resume the {@link ImageLoader} from pause state.
     *
     * @see #pause()
     * @see LoaderState
     */
    @LoaderApi
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
    @LoaderApi
    public void clearTasks() {
        ensureNotTerminated();
        mTaskManager.clearTasks();
    }

    /**
     * Terminate the loader.
     */
    @LoaderApi
    @Override
    public void terminate() {
        ensureNotTerminated();
        mState = LoaderState.TERMINATED;
        mTaskHandleService.terminate();
        mLoadingService.shutdown();
        mTaskManager.terminate();
        cancelAllTasks();
        mLogger.funcExit();
    }

    private void ensureNotTerminated() {
        Preconditions.checkState(!isTerminated(), "Loader has already been terminated");
    }

    /**
     * Clear all the load and loading tasks.
     */
    @LoaderApi
    public void cancelAllTasks() {
        synchronized (mFutures) {
            for (FutureImageTask futureImageTask : mFutures) {
                futureImageTask.cancel(true);
            }
            mFutures.clear();
        }
    }

    /**
     * Cancel the load and loading task who's from match given.
     *
     * @param url The from of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(@NonNull String url) {
        Preconditions.checkNotNull(url);
        List<FutureImageTask> pendingCancels = findTasks(url);
        if (pendingCancels.size() > 0) {
            for (FutureImageTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                mUiThreadRouter.callOnCancel(toBeCanceled.getListenableTask().getProgressListener());
                mLogger.info("Cancel task for from:" + url);
            }
            pendingCancels.clear();
        }
        return this;
    }

    /**
     * Cancel the load and loading task who's view match given.
     *
     * @param view The view of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(@NonNull ImageView view) {
        return cancel(new BitmapImageViewDelegate(view));
    }

    /**
     * Cancel the load and loading task who's settable match given.
     *
     * @param settable The settable of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(@NonNull ImageSeat settable) {
        return cancel(mSettableIdCreator.createSettableId(settable));
    }

    /**
     * Cancel the load and loading task who's settableId match given.
     *
     * @param settableId The settableId of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(long settableId) {
        Preconditions.checkState(settableId != 0, "Invalid settable with id:0");
        List<FutureImageTask> pendingCancels = findTasks(settableId);
        if (pendingCancels.size() > 0) {
            for (FutureImageTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                mUiThreadRouter.callOnCancel(toBeCanceled.getListenableTask().getProgressListener());
                mLogger.verbose("Cancel task for settable:" + settableId);
            }
            pendingCancels.clear();
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
    public boolean shouldRun(@NonNull DisplayTask<Bitmap> task) {
        if (!checkInRunningState()) return false;

        boolean isTaskDirty = mTaskManager.interruptExecute(task.getTaskRecord());
        if (isTaskDirty) {
            mLogger.verbose("Won't run task:" + task);
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

    @LoaderApi
    public long getInternalStorageUsage() {
        return StorageStats.from(mContext).getInternalStorageUsage();
    }

    @LoaderApi
    public long getExternalStorageUsage() {
        return StorageStats.from(mContext).getExternalStorageUsage();
    }

    @LoaderApi
    public long getTotalStorageUsage() {
        return StorageStats.from(mContext).getTotalStorageUsage();
    }

    @LoaderApi
    public long getTotalTrafficUsage() {
        return TrafficStats.from(mContext).getTotalTrafficUsage();
    }

    @LoaderApi
    public long getMobileTrafficUsage() {
        return TrafficStats.from(mContext).getMobileTrafficUsage();
    }

    @LoaderApi
    public long getWifiTrafficUsage() {
        return TrafficStats.from(mContext).getWifiTrafficUsage();
    }

    private class TaskHandler implements RequestHandler<FutureImageTask> {
        @Override
        public boolean handleRequest(FutureImageTask request) {
            freezeIfRequested();
            if (!onFutureSubmit(request)) return false;
            getExecutor(ImageSourceType.of(request.getListenableTask().getUrl())).submit(request);
            return true;
        }
    }

    private class TransactionHandler implements RequestHandler<Transaction> {

        @Override
        public boolean handleRequest(Transaction transaction) {
            transaction.startAsync();
            return true;
        }
    }
}
