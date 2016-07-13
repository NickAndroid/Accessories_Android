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
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.control.Freezer;
import dev.nick.imageloader.control.LoaderState;
import dev.nick.imageloader.display.BitmapImageSettings;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.ImageQuality;
import dev.nick.imageloader.display.ImageSettable;
import dev.nick.imageloader.display.ImageSettableIdCreator;
import dev.nick.imageloader.display.ImageSettableIdCreatorImpl;
import dev.nick.imageloader.display.ImageViewDelegate;
import dev.nick.imageloader.display.ResImageSettings;
import dev.nick.imageloader.display.animator.ImageAnimator;
import dev.nick.imageloader.display.processor.BitmapProcessor;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.imageloader.loader.task.ImageTask;
import dev.nick.imageloader.loader.task.ImageTaskImpl;
import dev.nick.imageloader.loader.task.ImageTaskRecord;
import dev.nick.imageloader.loader.task.TaskManager;
import dev.nick.imageloader.loader.task.TaskManagerImpl;
import dev.nick.imageloader.loader.task.TaskMonitor;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import dev.nick.stack.RequestHandler;
import dev.nick.stack.RequestStackService;

/**
 * Main class of {@link ImageLoader} library.
 */
public class ImageLoader implements TaskMonitor, Handler.Callback, RequestHandler<ImageTaskImpl> {

    private static final int MSG_APPLY_IMAGE_SETTINGS = 0x1;
    private static final int MSG_CALL_ON_START = 0x2;
    private static final int MSG_CALL_PROGRESS_UPDATE = 0x3;
    private static final int MSG_CALL_ON_COMPLETE = 0x4;
    private static final int MSG_CALL_ON_FAILURE = 0x5;

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private LoaderConfig mConfig;

    private RequestStackService<ImageTaskImpl> mStackService;

    private Logger mLogger;

    private final boolean DEBUG;

    private long mClearTaskRequestedTimeMills;

    private final Map<Integer, ImageTaskRecord> mTaskLockMap;

    private ExecutorService mLoadingService;
    private ExecutorService mImageSettingsScheduler;

    private Freezer mFreezer;
    private LoaderState mState;

    private TaskManager mTaskManager;
    private ImageSettableIdCreator mSettableIdCreator;

    private static ImageLoader sLoader;

    private ImageLoader(Context context, LoaderConfig config) {
        this.mContext = context;
        this.mConfig = config;
        mTaskManager = new TaskManagerImpl();
        mSettableIdCreator = new ImageSettableIdCreatorImpl();
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        this.mCacheManager = new CacheManager(config.getCachePolicy(), context);
        this.mLoadingService = Executors.newFixedThreadPool(config.getLoadingThreads());
        this.mImageSettingsScheduler = Executors.newSingleThreadExecutor();
        this.mStackService = RequestStackService.createStarted(this);
        this.mTaskLockMap = new HashMap<>();
        this.mState = LoaderState.RUNNING;
        this.mLogger = LoggerManager.getLogger(getClass());
        this.DEBUG = BuildConfig.DEBUG;
    }

    /**
     * Init the image loader with default {@link LoaderConfig}.
     *
     * @param context An application {@link Context} is preferred.
     */
    public synchronized static void init(Context context) {
        init(context, LoaderConfig.DEFAULT_CONFIG);
    }

    /**
     * Init the image loader with custom {@link LoaderConfig}.
     *
     * @param context An application {@link Context} is preferred.
     * @param config  Configuration of this loader.
     * @see LoaderConfig
     */
    public synchronized static void init(Context context, LoaderConfig config) {
        if (config == null) throw new NullPointerException("config is null.");
        if (sLoader == null) {
            sLoader = new ImageLoader(context, config);
            return;
        }
        throw new IllegalArgumentException("Already configured.");
    }

    /**
     * @return Single instance of {@link ImageLoader}
     */
    public static ImageLoader getInstance() {
        return sLoader;
    }

    /**
     * Clear all pending tasks.
     */
    public void clearTasks() {
        mClearTaskRequestedTimeMills = System.currentTimeMillis();
    }

    /**
     * Display image from the url to the view.
     *
     * @param url  Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param view Target view to display the image.
     */
    public void displayImage(String url, ImageView view) {
        ImageViewDelegate viewDelegate = new ImageViewDelegate(view);
        displayImage(url, viewDelegate, null);
    }

    /**
     * Display image from the url to the view.
     *
     * @param url    Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param view   Target view to display the image.
     * @param option {@link DisplayOption} is options using when display the image.
     */
    public void displayImage(String url, ImageView view, DisplayOption option) {
        displayImage(url, view, option, null);
    }

    /**
     * Display image from the url to the view.
     *
     * @param url    Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param view   Target view to display the image.
     * @param option {@link DisplayOption} is options using when display the image.
     */
    public void displayImage(String url, ImageView view, DisplayOption option, LoadingListener loadingListener) {
        ImageViewDelegate viewDelegate = new ImageViewDelegate(view);
        displayImage(url, viewDelegate, option, loadingListener);
    }

    /**
     * Display image from the url to the view.
     *
     * @param url      Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param settable Target {@link ImageSettable} to display the image.
     */
    public void displayImage(String url, ImageSettable settable) {
        displayImage(url, settable, null);
    }

    /**
     * Display image from the url to the view.
     *
     * @param url      Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param settable Target {@link ImageSettable} to display the image.
     * @param option   {@link DisplayOption} is options using when display the image.
     */
    public void displayImage(String url,
                             ImageSettable settable,
                             DisplayOption option) {
        displayImage(url, settable, option, null);
    }

    /**
     * Display image from the url to the view.
     *
     * @param url      Image source url, one of {@link dev.nick.imageloader.loader.ImageSource}
     * @param settable Target {@link ImageSettable} to display the image.
     * @param option   {@link DisplayOption} is options using when display the image.
     * @param listener The progress listener using to watch the progress of the loading.
     */
    public void displayImage(String url,
                             ImageSettable settable,
                             DisplayOption option,
                             LoadingListener listener) {

        beforeLoading(settable, option);

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        ViewSpec info = new ViewSpec(settable.getWidth(), settable.getHeight());

        if (mCacheManager.isMemCacheEnabled()) {
            Bitmap cached;
            if ((cached = mCacheManager.getMemCache(url, info)) != null) {
                if (DEBUG) mLogger.verbose("Using cached mem bitmap:" + cached);
                applyImageSettings(cached,
                        option == null ? null : option.getProcessor(),
                        settable, option == null ? null : option.getAnimator());
                return;
            }
        }

        String loadingUrl = url;

        if (mCacheManager.isDiskCacheEnabled()) {
            String cachePath;
            if ((cachePath = mCacheManager.getDiskCachePath(url, info)) != null) {
                if (DEBUG) mLogger.verbose("Using cached disk cache:" + cachePath);
                loadingUrl = ImageSource.FILE.getPrefix() + cachePath;
            }
        }

        startLoading(loadingUrl, settable, option, listener);
    }

    private void beforeLoading(ImageSettable settable, DisplayOption option) {
        int showWhenLoading = 0;
        if (option != null) {
            showWhenLoading = option.getLoadingImgRes();
        }
        applyImageSettings(showWhenLoading, settable, null);
    }

    private void startLoading(String url,
                              ImageSettable settable,
                              DisplayOption option,
                              LoadingListener listener) {

        option = createOptionIfNull(option);

        ImageQuality imageQuality = option.getQuality();

        ViewSpec viewSpec = new ViewSpec(settable.getWidth(), settable.getHeight());

        int settableId = mSettableIdCreator.createSettableId(settable);
        int taskId = mTaskManager.nextTaskId();


        ImageTaskRecord imageTaskRecord = new ImageTaskRecord(settableId, taskId);

        ProgressListenerDelegate progressListenerDelegate = new ProgressListenerDelegate(
                listener,
                viewSpec,
                option,
                settable,
                imageTaskRecord,
                url);

        ErrorListenerDelegate errorListenerDelegate = null;

        if (listener != null) {
            errorListenerDelegate = new ErrorListenerDelegate(listener);
        }

        ImageTaskImpl task = new ImageTaskImpl(
                mContext,
                mConfig,
                this,
                url,
                viewSpec,
                imageQuality,
                progressListenerDelegate,
                errorListenerDelegate,
                imageTaskRecord);

        onTaskCreated(imageTaskRecord);

        // Push it to the request stack.
        mStackService.push(task);
    }

    private DisplayOption createOptionIfNull(DisplayOption option) {
        if (option != null) return option;
        return new DisplayOption.Builder()
                .imageQuality(ImageQuality.FIT_VIEW)
                .imageAnimator(null)
                .bitmapProcessor(null)
                .defaultImgRes(0)
                .loadingImgRes(0)
                .viewMaybeReused()
                .build();
    }

    private void onTaskCreated(ImageTaskRecord record) {
        if (DEBUG)
            mLogger.verbose("Created task:" + record);
        int taskId = record.getTaskId();
        int settableId = record.getSettableId();
        synchronized (mTaskLockMap) {
            ImageTaskRecord exists = mTaskLockMap.get(settableId);
            if (exists != null) {
                exists.setTaskId(taskId);
            } else {
                mTaskLockMap.put(settableId, record);
            }
        }
    }

    private boolean isTaskDirty(ImageTaskRecord task) {

        boolean outDated = task.upTime() <= mClearTaskRequestedTimeMills;

        if (outDated) {
            return true;
        }

        synchronized (mTaskLockMap) {
            ImageTaskRecord lock = mTaskLockMap.get(task.getSettableId());
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
    private void applyImageSettings(Bitmap bitmap, BitmapProcessor processor, ImageSettable settable,
                                    ImageAnimator animator) {
        if (settable != null) {
            if (DEBUG) {
                mLogger.debug("applyImageSettings, Bitmap:"
                        + bitmap
                        + ", for settle:"
                        + mSettableIdCreator.createSettableId(settable));
            }
            BitmapImageSettings settings = new BitmapImageSettings(animator,
                    (processor == null ? bitmap : processor.process(bitmap)), settable);
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    @WorkerThread
    private void applyImageSettings(int resId, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            if (DEBUG) {
                mLogger.debug("applyImageSettings, Res:" + resId + ", for settle:"
                        + mSettableIdCreator.createSettableId(settable));
            }
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
            case MSG_CALL_PROGRESS_UPDATE:
                onCallOnProgressUpdate((ProgressListener<BitmapResult>) message.obj, message.arg1);
                break;
        }
        return true;
    }

    private void callOnStart(ProgressListener listener) {
        if (listener != null) {
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_START, listener).sendToTarget();
        }
    }

    private void callOnProgressUpdate(ProgressListener listener, int progress) {
        if (listener != null) {
            mUIThreadHandler.obtainMessage(MSG_CALL_PROGRESS_UPDATE, progress, 0, listener).sendToTarget();
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

    private void onCallOnProgressUpdate(ProgressListener<BitmapResult> listener, int progress) {
        listener.onProgressUpdate(progress);
    }

    private void onCallOnComplete(CompleteParams params) {
        params.progressListener.onComplete(params.result);
    }

    private void onCallOnFailure(FailureParams params) {
        params.listener.onError(params.cause);
    }

    @Override
    public boolean handle(ImageTaskImpl request) {
        mLoadingService.execute(request);
        return true;
    }

    public void pause() {
        if (mState == LoaderState.TERMINATED) {
            throw new IllegalStateException("Loader has been terminated.");
        }
        mState = LoaderState.PAUSE_REQUESTED;
        mLogger.funcExit();
    }

    public boolean isPaused() {
        return mState == LoaderState.PAUSED || mState == LoaderState.PAUSE_REQUESTED;
    }

    public void resume() {
        if (mState == LoaderState.TERMINATED) {
            throw new IllegalStateException("Loader has been terminated.");
        }
        mFreezer.resume();
        mState = LoaderState.RUNNING;
        mLogger.funcExit();
    }

    public void terminate() {
        if (mState == LoaderState.TERMINATED) {
            throw new IllegalStateException("Loader has already been terminated.");
        }
        mState = LoaderState.TERMINATED;
        mStackService.terminate();
        mLoadingService.shutdown();
        synchronized (mTaskLockMap) {
            mTaskLockMap.clear();
        }
        mLogger.funcExit();
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
        mLogger.funcExit();
    }

    @WorkerThread
    public void clearMemCache() {
        mCacheManager.evictMem();
        mLogger.funcExit();
    }

    @Override
    public boolean shouldRun(@NonNull ImageTask task) {
        if (!checkInRunningState()) return false;
        // Check if this task is dirty.
        boolean isTaskDirty = isTaskDirty(task.getTaskRecord());
        if (isTaskDirty) {
            if (DEBUG) mLogger.info("Won't run task:" + task);
            return false;
        }
        return true;
    }

    boolean checkInRunningState() {
        if (mState == LoaderState.TERMINATED) {
            return false;
        }
        if (mState == LoaderState.PAUSE_REQUESTED) {
            mState = LoaderState.PAUSED;
            if (mFreezer == null) mFreezer = new Freezer();
            mLogger.debug("Pausing the loader...");
            mFreezer.freeze();
        }
        return true;
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

        private ImageTaskRecord taskRecord;

        public ProgressListenerDelegate(ProgressListener<BitmapResult> listener,
                                        ViewSpec viewSpec,
                                        DisplayOption option,
                                        @NonNull ImageSettable settable,
                                        ImageTaskRecord taskRecord,
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
            callOnStart(listener);
        }

        @Override
        public void onProgressUpdate(int progress) {
            callOnProgressUpdate(listener, progress);
        }

        @Override
        public void onComplete(final BitmapResult result) {

            if (result.result == null) {
                return;
            }

            final boolean isViewMaybeReused = option.isViewMaybeReused();

            if (!isViewMaybeReused || !isTaskDirty(taskRecord)) {
                if (!option.isApplyImageOneByOne()) {
                    ImageAnimator animator = (option == null ? null : option.getAnimator());
                    BitmapProcessor processor = (option == null ? null : option.getProcessor());
                    applyImageSettings(result.result, processor, settable, animator);
                    return;
                }
                mImageSettingsScheduler.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewMaybeReused && isTaskDirty(taskRecord)) return;
                        ImageAnimator animator = (option == null ? null : option.getAnimator());
                        BitmapProcessor processor = (option == null ? null : option.getProcessor());
                        applyImageSettings(result.result, processor, settable, animator);
                        if (animator != null) {
                            long delay = animator.getDuration();
                            ImageSettingsLocker locker = new ImageSettingsLocker(delay / 3);
                            locker.lock();
                        }
                    }
                });
            } else if (DEBUG) {
                mLogger.info("Won't apply image settings for task:" + taskRecord);
            }

            mCacheManager.cache(url, viewSpec, result.result);

            callOnComplete(listener, result);
        }
    }

    private class ErrorListenerDelegate implements ErrorListener {

        ErrorListener listener;

        public ErrorListenerDelegate(ErrorListener listener) {
            this.listener = listener;
        }

        @Override
        public void onError(@NonNull Cause cause) {
            callOnFailure(listener, cause);
        }
    }

    private static class FailureParams {
        Cause cause;
        ErrorListener listener;
    }

    private static class CompleteParams {
        BitmapResult result;
        ProgressListener<BitmapResult> progressListener;
    }
}
