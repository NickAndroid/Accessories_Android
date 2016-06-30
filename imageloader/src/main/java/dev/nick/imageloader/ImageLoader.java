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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.display.BitmapImageSettings;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.ImageSettable;
import dev.nick.imageloader.display.ImageViewDelegate;
import dev.nick.imageloader.display.ResImageSettings;
import dev.nick.imageloader.display.animator.ImageAnimator;
import dev.nick.imageloader.display.processor.BitmapProcessor;
import dev.nick.imageloader.loader.ImageInfo;
import dev.nick.imageloader.loader.task.LoadingTask;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import dev.nick.stack.RequestHandler;
import dev.nick.stack.RequestStackService;

/**
 * Main class of {@link ImageLoader} library.
 */
public class ImageLoader implements Handler.Callback, RequestHandler<LoadingTask> {

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private LoaderConfig mConfig;

    private RequestStackService<LoadingTask> mStackService;

    private Logger mLogger;

    private final boolean DEBUG;

    private final AtomicInteger mTaskId = new AtomicInteger(0);

    private final Map<Integer, TaskLock> mTaskLockMap;

    private ExecutorService mLoadingService;

    private static ImageLoader sLoader;

    private ImageLoader(Context context, LoaderConfig config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        this.mCacheManager = new CacheManager(config, context);
        this.mLoadingService = Executors.newFixedThreadPool(config.getLoadingThreads());
        this.mStackService = RequestStackService.createStarted(this);
        this.mTaskLockMap = new HashMap<>();
        this.mLogger = LoggerManager.getLogger(getClass());
        this.DEBUG = config.isDebug();
    }

    /**
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

    public void displayImage(String url, ImageView view) {
        ImageViewDelegate viewDelegate = new ImageViewDelegate(new WeakReference<>(view));
        displayImage(url, viewDelegate, null);
    }

    public void displayImage(String url, ImageView view, DisplayOption option) {
        ImageViewDelegate viewDelegate = new ImageViewDelegate(new WeakReference<>(view));
        displayImage(url, viewDelegate, option);
    }

    public void displayImage(String url, ImageSettable settable) {
        displayImage(url, settable, null);
    }


    public void displayImage(final String url,
                             final ImageSettable settable,
                             final DisplayOption option) {
        // 1. Get from cache.
        // 2. If no cache, start a loading task.
        // 3. Cache the loaded.
        if (mConfig.isMemCacheEnabled() || mConfig.isDiskCacheEnabled()) {
            ImageInfo info = new ImageInfo(settable.getWidth(), settable.getHeight());
            mCacheManager.get(url, info, new CacheManager.Callback() {
                @Override
                public void onResult(final Bitmap cached) {
                    if (cached != null) {
                        if (DEBUG) mLogger.info("Using cached bitmap:" + cached);
                        postApplyImageSettings(cached,
                                option == null ? null : option.getProcessor(),
                                settable, option == null ? null : option.getAnimator());
                    } else {
                        startLoading(url, settable, option);
                    }
                }
            });
        } else {
            startLoading(url, settable, option);
        }
    }

    private void startLoading(final String url,
                              final ImageSettable settable,
                              final DisplayOption option) {

        ImageInfo info = new ImageInfo(settable.getWidth(), settable.getHeight());

        int viewId = createIdOfImageSettable(settable);
        int taskId = nextTaskId();

        LoadingTask.TaskCallback<Bitmap> callback = new ImageTaskCallback(settable, option, url, info);

        LoadingTask task = new LoadingTask(mContext, callback, taskId, viewId, info, url);
        onTaskCreated(viewId, task);
        mStackService.push(task);
    }

    private int createIdOfImageSettable(ImageSettable view) {
        return view.hashCode();
    }

    private void postApplyImageSettings(Bitmap bitmap, BitmapProcessor processor, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            BitmapImageSettings settings = new BitmapImageSettings(animator,
                    new WeakReference<>(processor == null ? bitmap : processor.process(bitmap)), settable);
            mUIThreadHandler.obtainMessage(0, settings).sendToTarget();
        }
    }

    private void postApplyImageSettings(int resId, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            ResImageSettings settings = new ResImageSettings(animator, resId, settable);
            mUIThreadHandler.obtainMessage(0, settings).sendToTarget();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        Runnable settings = (Runnable) message.obj;
        settings.run();
        return true;
    }

    private int nextTaskId() {
        return mTaskId.getAndIncrement();
    }

    private void onTaskCreated(int settableId, LoadingTask task) {
        if (DEBUG)
            mLogger.info("Created task, settable:" + settableId + ", tid:" + task.getTaskId());
        int taskId = task.getTaskId();
        synchronized (mTaskLockMap) {
            TaskLock exists = mTaskLockMap.get(settableId);
            if (exists != null) {
                exists.taskId = taskId;
            } else {
                TaskLock lock = new TaskLock(taskId);
                mTaskLockMap.put(settableId, lock);
            }
        }
    }

    private boolean isTaskDirty(LoadingTask task) {
        synchronized (mTaskLockMap) {
            TaskLock lock = mTaskLockMap.get(task.getSettableId());
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

    @Override
    public boolean handle(LoadingTask request) {
        mLoadingService.execute(request);
        return true;
    }

    public void terminate() {
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

    class ImageTaskCallback implements LoadingTask.TaskCallback<Bitmap> {

        @NonNull
        ImageSettable settable;
        String url;
        DisplayOption option;
        ImageInfo info;

        public ImageTaskCallback(@NonNull ImageSettable settable,
                                 DisplayOption option, String url, ImageInfo info) {
            this.option = option;
            this.url = url;
            this.info = info;
            this.settable = settable;
        }

        @Override
        public boolean onPreStart(LoadingTask task) {
            // Check if this task is dirty.
            boolean isTaskDirty = isTaskDirty(task);
            if (isTaskDirty) {
                if (DEBUG) mLogger.info("Won't run task, id" + task.getTaskId());
                return false;
            }
            if (option != null) {
                int showWhenLoading = option.getLoadingImgRes();
                if (showWhenLoading > 0)
                    postApplyImageSettings(showWhenLoading, settable, null);
            } else {
                postApplyImageSettings(null, null, settable, null);
            }
            return true;
        }

        @Override
        public void onComplete(Bitmap result, LoadingTask task) {
            if (result == null) {
                if (DEBUG) mLogger.warn("No image got");
                onNoImageGot();
                return;
            }
            if (!isTaskDirty(task)) {
                postApplyImageSettings(result, option == null ? null : option.getProcessor(), settable,
                        option == null ? null : option.getAnimator());
            } else if (DEBUG) {
                mLogger.info("Won't apply image settings for task:" + task.getTaskId());
            }
            mCacheManager.cache(url, info, result);
        }

        @Override
        public void onError(String errMsg) {
            if (mConfig.isDebug()) mLogger.error(errMsg);
            onNoImageGot();
        }

        void onNoImageGot() {
            if (option != null) {
                int defaultImgRes = option.getDefaultImgRes();
                if (defaultImgRes > 0)
                    postApplyImageSettings(defaultImgRes, settable, null);
            }
        }
    }

    class TaskLock {
        int taskId;

        TaskLock(int taskId) {
            this.taskId = taskId;
        }
    }
}
