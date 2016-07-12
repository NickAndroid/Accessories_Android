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
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.control.Freezer;
import dev.nick.imageloader.control.LoaderState;
import dev.nick.imageloader.display.BitmapImageSettings;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.ImageSettable;
import dev.nick.imageloader.display.ImageViewDelegate;
import dev.nick.imageloader.display.ResImageSettings;
import dev.nick.imageloader.display.animator.ImageAnimator;
import dev.nick.imageloader.display.processor.BitmapProcessor;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ImageSpec;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.FailedCause;
import dev.nick.imageloader.loader.task.BitmapLoadingTask;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import dev.nick.stack.RequestHandler;
import dev.nick.stack.RequestStackService;

/**
 * Main class of {@link ImageLoader} library.
 */
public class ImageLoader implements Handler.Callback, RequestHandler<BitmapLoadingTask> {

    private static final int MSG_APPLY_IMAGE_SETTINGS = 0x1;
    private static final int MSG_CALL_ON_START = 0x2;
    private static final int MSG_CALL_PROGRESS_UPDATE = 0x3;
    private static final int MSG_CALL_ON_COMPLETE = 0x4;
    private static final int MSG_CALL_ON_FAILURE = 0x5;

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private LoaderConfig mConfig;

    private RequestStackService<BitmapLoadingTask> mStackService;

    private Logger mLogger;

    private final boolean DEBUG;

    private final AtomicInteger mTaskId = new AtomicInteger(0);

    private long mClearTaskRequestedTimeMills;

    private final Map<Integer, TaskRecord> mTaskLockMap;

    private ExecutorService mLoadingService;
    private ExecutorService mImageSettingsSchduler;

    private Freezer mFreezer;
    private LoaderState mState;

    private static ImageLoader sLoader;

    private ImageLoader(Context context, LoaderConfig config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        this.mCacheManager = new CacheManager(config, context);
        this.mLoadingService = Executors.newFixedThreadPool(config.getLoadingThreads());
        this.mImageSettingsSchduler = Executors.newSingleThreadExecutor();
        this.mStackService = RequestStackService.createStarted(this);
        this.mTaskLockMap = new HashMap<>();
        this.mState = LoaderState.RUNNING;
        this.mLogger = LoggerManager.getLogger(getClass());
        this.DEBUG = config.isDebug();
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
        ImageViewDelegate viewDelegate = new ImageViewDelegate(view);
        displayImage(url, viewDelegate, option);
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
                             ProgressListener listener) {

        beforeLoading(settable, option);

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        ImageSpec info = new ImageSpec(settable.getWidth(), settable.getHeight());
        if (mConfig.isMemCacheEnabled()) {
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

        if (mConfig.isDiskCacheEnabled()) {
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
                              ProgressListener listener) {

        option = createOptionIfNull(option);

        DisplayOption.ImageQuality quality = option.getQuality();

        ImageSpec spec = new ImageSpec(settable.getWidth(), settable.getHeight());

        int viewId = getIdOfImageSettable(settable);
        int taskId = nextTaskId();

        BitmapLoadingTask.TaskCallback<BitmapResult> callback = new ImageTaskCallback(settable, option, url, spec, listener);

        BitmapLoadingTask task = new BitmapLoadingTask(mContext, callback, mConfig, taskId, viewId, spec, quality, url);

        onTaskCreated(viewId, task);

        // Push it to the request stack.
        mStackService.push(task);
    }

    private DisplayOption createOptionIfNull(DisplayOption option) {
        if (option != null) return option;
        return new DisplayOption.Builder()
                .imageQuality(DisplayOption.ImageQuality.FIT_VIEW)
                .imageAnimator(null)
                .bitmapProcessor(null)
                .defaultImgRes(0)
                .loadingImgRes(0)
                .viewMaybeReused()
                .build();
    }

    private int getIdOfImageSettable(ImageSettable view) {
        return view.hashCode();
    }

    private int nextTaskId() {
        return mTaskId.getAndIncrement();
    }

    private void onTaskCreated(int settableId, BitmapLoadingTask task) {
        if (DEBUG)
            mLogger.verbose("Created task, settable:" + settableId + ", tid:" + task.getTaskId());
        int taskId = task.getTaskId();
        synchronized (mTaskLockMap) {
            TaskRecord exists = mTaskLockMap.get(settableId);
            if (exists != null) {
                exists.taskId = taskId;
            } else {
                TaskRecord lock = new TaskRecord(taskId);
                mTaskLockMap.put(settableId, lock);
            }
        }
    }

    private boolean isTaskDirty(BitmapLoadingTask task) {

        boolean outDated = task.getUpTime() <= mClearTaskRequestedTimeMills;

        if (outDated) {
            return true;
        }

        synchronized (mTaskLockMap) {
            TaskRecord lock = mTaskLockMap.get(task.getSettableId());
            if (lock != null) {
                int taskId = lock.taskId;
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
            if (DEBUG)
                mLogger.debug("applyImageSettings, Bitmap:" + bitmap + ", for settle:" + getIdOfImageSettable(settable));
            BitmapImageSettings settings = new BitmapImageSettings(animator,
                    (processor == null ? bitmap : processor.process(bitmap)), settable);
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    @WorkerThread
    private void applyImageSettings(int resId, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            if (DEBUG)
                mLogger.debug("applyImageSettings, Res:" + resId + ", for settle:"
                        + getIdOfImageSettable(settable));
            ResImageSettings settings = new ResImageSettings(animator, resId, settable);
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_APPLY_IMAGE_SETTINGS:
                onApplyImageSettings((Runnable) message.obj);
                break;
            case MSG_CALL_ON_START:
                onCallOnStart((ProgressListener) message.obj);
                break;
            case MSG_CALL_ON_COMPLETE:
                onCallOnComplete((ProgressListener) message.obj);
                break;
            case MSG_CALL_ON_FAILURE:
                onCallOnFailure((FailureParams) message.obj);
                break;
            case MSG_CALL_PROGRESS_UPDATE:
                onCallOnProgressUpdate((ProgressListener) message.obj, message.arg1);
                break;
        }
        return true;
    }

    private void callOnStart(ProgressListener listener) {
        mUIThreadHandler.obtainMessage(MSG_CALL_ON_START, listener).sendToTarget();
    }

    private void callOnProgressUpdate(ProgressListener listener, int progress) {
        mUIThreadHandler.obtainMessage(MSG_CALL_PROGRESS_UPDATE, progress, 0, listener).sendToTarget();
    }

    private void callOnComplete(ProgressListener listener) {
        mUIThreadHandler.obtainMessage(MSG_CALL_ON_COMPLETE, listener).sendToTarget();
    }

    private void callOnFailure(ProgressListener listener, FailedCause cause) {
        FailureParams failureParams = new FailureParams();
        failureParams.cause = cause;
        failureParams.listener = listener;
        mUIThreadHandler.obtainMessage(MSG_CALL_ON_FAILURE, failureParams).sendToTarget();
    }

    private void onCallOnStart(ProgressListener listener) {
        listener.onStart();
    }

    private void onCallOnProgressUpdate(ProgressListener listener, int progress) {
        listener.onProgressUpdate(progress);
    }

    private void onCallOnComplete(ProgressListener listener) {
        listener.onComplete();
    }

    private void onCallOnFailure(FailureParams params) {
        params.listener.onFailure(params.cause);
    }

    @Override
    public boolean handle(BitmapLoadingTask request) {
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

    class ImageTaskCallback implements BitmapLoadingTask.TaskCallback<BitmapResult> {

        @NonNull
        ImageSettable settable;
        String url;
        DisplayOption option;
        ImageSpec info;
        @Nullable
        ProgressListener listener;

        public ImageTaskCallback(@NonNull ImageSettable settable,
                                 DisplayOption option,
                                 String url,
                                 ImageSpec info,
                                 @Nullable ProgressListener listener) {
            this.option = option;
            this.url = url;
            this.info = info;
            this.settable = settable;
            this.listener = listener;
        }

        @Override
        public boolean onPreStart(BitmapLoadingTask task) {
            if (!checkState()) return false;
            // Check if this task is dirty.
            boolean isTaskDirty = isTaskDirty(task);
            if (isTaskDirty) {
                if (DEBUG) mLogger.info("Won't run task, id" + task.getTaskId());
                return false;
            }
            if (listener != null) callOnStart(listener);
            beforeLoading(settable, option);
            return true;
        }

        @Override
        public void onComplete(final BitmapResult result, final BitmapLoadingTask task) {
            if (result.result == null) {
                onNoImageGot(result.cause);
                return;
            }

            callOnComplete(listener);

            if (!option.isViewMaybeReused() || !isTaskDirty(task)) {
                if (!option.isApplyImageOneByOne()) {
                    ImageAnimator animator = (option == null ? null : option.getAnimator());
                    BitmapProcessor processor = (option == null ? null : option.getProcessor());
                    applyImageSettings(result.result, processor, settable, animator);
                    return;
                }
                mImageSettingsSchduler.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isTaskDirty(task)) return;
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
                mLogger.info("Won't apply image settings for task:" + task.getTaskId());
            }
            mCacheManager.cache(url, info, result.result);
        }

        void onNoImageGot(FailedCause cause) {
            if (DEBUG) mLogger.warn("No image got, calling back, failed cause:" + cause);
            if (listener != null) callOnFailure(listener, cause);
            if (option != null) {
                int defaultImgRes = option.getDefaultImgRes();
                if (defaultImgRes > 0) {
                    applyImageSettings(defaultImgRes, settable, null);
                }
            }
        }

        boolean checkState() {
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
    }

    class TaskRecord {
        int taskId;

        TaskRecord(int taskId) {
            this.taskId = taskId;
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

    public interface ProgressListener {
        void onStart();

        void onProgressUpdate(int progress);

        void onComplete();

        void onFailure(FailedCause cause);
    }

    public static class ProgressListenerStub implements ProgressListener {

        @Override
        public void onStart() {
            // To be impl.
        }

        @Override
        public void onProgressUpdate(int progress) {
            // To be impl.
        }

        @Override
        public void onComplete() {
            // To be impl.
        }

        @Override
        public void onFailure(FailedCause cause) {
            // To be impl.
        }
    }

    private static class FailureParams {
        FailedCause cause;
        ProgressListener listener;
    }
}
