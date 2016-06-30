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
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.display.BitmapImageSettings;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.FadeInImageAnimator;
import dev.nick.imageloader.display.ImageAnimator;
import dev.nick.imageloader.display.ImageSettable;
import dev.nick.imageloader.display.ImageViewDelegate;
import dev.nick.imageloader.display.ResImageSettings;
import dev.nick.imageloader.loader.ImageInfo;
import dev.nick.imageloader.loader.task.LoadingTask;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import dev.nick.stack.RequestHandler;
import dev.nick.stack.RequestStackService;

public class ImageLoader implements Handler.Callback {

    static final String LOG_TAG = "ImageLoader";

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private ImageAnimator mDefaultImageAnimator;

    private Config mConfig;

    private RequestStackService<LoadingTask> mStackService;

    private Logger mLogger;

    private final AtomicInteger mTaskId = new AtomicInteger(0);

    private final Map<Integer, TaskLock> mTaskLockMap;

    private ExecutorService mLoadingService;

    private static ImageLoader sLoader;

    private ImageLoader(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        this.mCacheManager = new CacheManager(config, context);
        this.mLoadingService = new ThreadPoolExecutor(
                config.loadingThreads,
                config.loadingThreads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(config.loadingThreads), null, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                
            }
        });
        this.mStackService = RequestStackService.createStarted(
                new RequestHandler<LoadingTask>() {
                    @Override
                    public boolean handle(LoadingTask request) {
                        synchronized (mTaskLockMap) {
                            TaskLock lock = mTaskLockMap.get(request.getSettableId());
                            if (lock != null) {
                                int taskId = lock.taskId;
                                if (taskId > request.getTaskId()) {
                                    mLogger.info("Won't run task, id" + request.getTaskId() + ", lock id:" + taskId);
                                    return false;
                                }
                            }
                        }
                        mLoadingService.execute(request);
                        // request.run();
                        return true;
                    }
                });
        this.mTaskLockMap = new HashMap<>();
        this.mLogger = LoggerManager.getLogger(getClass());
    }

    public synchronized static void init(Context context, Config config) {
        if (sLoader == null) {
            sLoader = new ImageLoader(context, config);
            return;
        }
        throw new IllegalArgumentException("Already configured.");
    }

    public static ImageLoader getInstance() {
        return sLoader;
    }

    private ImageAnimator getDefaultAnimator() {
        if (mDefaultImageAnimator == null) mDefaultImageAnimator = new FadeInImageAnimator();
        return mDefaultImageAnimator;
    }

    public void displayImage(final String url, final ImageView view) {
        displayImage(url, view, getDefaultAnimator(), null);
    }

    public void displayImage(final String url, ImageView view, DisplayOption option) {
        displayImage(url, view, getDefaultAnimator(), option);
    }

    public void displayImage(final String url, ImageView view, ImageAnimator animator) {
        displayImage(url, view, animator, null);
    }

    public void displayImage(final String url,
                             final ImageView view,
                             final ImageAnimator animator,
                             final DisplayOption option) {
        // 1. Get from cache.
        // 2. If no cache, start a loading task.
        // 3. Cache the loaded.
        final ImageViewDelegate viewDelegate = new ImageViewDelegate(new WeakReference<>(view));
        if (mConfig.isEnableMemCache() || mConfig.isEnableFileCache()) {
            ImageInfo info = new ImageInfo(viewDelegate.getWidth(), viewDelegate.getHeight());
            mCacheManager.get(url, info, new CacheManager.Callback() {
                @Override
                public void onResult(final Bitmap cached) {
                    if (cached != null) {
                        mLogger.info("Using cached bitmap:" + cached);
                        applyImageSetting(cached,
                                viewDelegate,
                                getDefaultAnimator());// Do not animate when loaded from cache?
                    } else {
                        displayImageAfterLoaded(url, viewDelegate, animator, option);
                    }
                }
            });
        } else {
            displayImageAfterLoaded(url, viewDelegate, animator, option);
        }
    }

    private void displayImageAfterLoaded(final String url,
                                         final ImageSettable settable,
                                         final ImageAnimator animator,
                                         final DisplayOption option) {

        ImageInfo info = new ImageInfo(settable.getWidth(), settable.getHeight());

        int viewId = createIdOfImageSettable(settable);
        int taskId = nextTaskId();

        LoadingTask.TaskCallback<Bitmap> callback = new ImageTaskCallback(settable, animator, option, url, info);

        LoadingTask task = new LoadingTask(mContext, callback, taskId, viewId, info, url);
        onTaskCreated(viewId, task);
        mStackService.push(task);
    }

    private int createIdOfImageSettable(ImageSettable view) {
        return view.hashCode();
    }

    private void applyImageSetting(Bitmap bitmap, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            BitmapImageSettings settings = new BitmapImageSettings(animator, new WeakReference<>(bitmap), settable);
            mUIThreadHandler.obtainMessage(0, settings).sendToTarget();
        }
    }

    private void applyImageSetting(int resId, ImageSettable settable, ImageAnimator animator) {
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

    int nextTaskId() {
        return mTaskId.getAndIncrement();
    }

    void onTaskCreated(int settableId, LoadingTask task) {
        mLogger.info("settable:" + settableId + ", tid:" + task.getTaskId());
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

    class ImageTaskCallback implements LoadingTask.TaskCallback<Bitmap> {

        @NonNull
        ImageSettable settable;
        ImageAnimator imageAnimator;
        String url;
        DisplayOption option;
        ImageInfo info;

        public ImageTaskCallback(@NonNull ImageSettable settable, ImageAnimator imageAnimator,
                                 DisplayOption option, String url, ImageInfo info) {
            this.imageAnimator = imageAnimator;
            this.option = option;
            this.url = url;
            this.info = info;
            this.settable = settable;
        }

        @Override
        public void onStart() {
            if (option != null) {
                int showWhenLoading = option.getImgResShowWhenLoading();
                if (showWhenLoading > 0)
                    applyImageSetting(showWhenLoading, settable, null);
            } else {
                applyImageSetting(null, settable, null);
            }
        }

        @Override
        public void onComplete(Bitmap result, int id) {
            if (result == null) {
                mLogger.warn("No image got");
                return;
            }
            int settableId = createIdOfImageSettable(settable);
            synchronized (mTaskLockMap) {
                TaskLock lock = mTaskLockMap.get(settableId);
                // InCase the view is reused.
                if (lock != null && lock.taskId == id) {
                    mLogger.info("Applying image settings for task:" + id + ", for settable:" + settableId);
                    applyImageSetting(result, settable, imageAnimator);
                } else {
                    mLogger.info("Won't apply image settings for task:" + id + ", need id:"
                            + (lock != null ? lock.taskId : 0) + ", for settable:" + settableId);
                }
            }
            mCacheManager.cache(url, info, result);
        }

        @Override
        public void onError(String errMsg) {
            if (option != null) {
                int showWhenError = option.getImgResShowWhenError();
                if (showWhenError > 0)
                    applyImageSetting(showWhenError, settable, null);
            }
            if (mConfig.isDebug()) mLogger.error(errMsg);
        }
    }

    public static class Config {

        boolean debug = true;
        boolean enableFileCache = true;
        boolean enableMemCache = true;
        boolean preferExternalStorageCache = true;

        int loadingThreads = Runtime.getRuntime().availableProcessors();
        int cacheThreads = loadingThreads / 2;

        public boolean isPreferExternalStorageCache() {
            return preferExternalStorageCache;
        }

        public Config setPreferExternalStorageCache(boolean preferExternalStorageCache) {
            this.preferExternalStorageCache = preferExternalStorageCache;
            return this;
        }

        public int getCacheThreads() {
            return cacheThreads;
        }

        public Config setCacheThreads(int cacheThreads) {
            this.cacheThreads = cacheThreads;
            return this;
        }

        public int getLoadingThreads() {
            return loadingThreads;
        }

        public Config setLoadingThreads(int loadingThreads) {
            this.loadingThreads = loadingThreads;
            return this;
        }

        public Config setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Config setEnableFileCache(boolean enableFileCache) {
            this.enableFileCache = enableFileCache;
            return this;
        }

        public Config setEnableMemCache(boolean enableMemCache) {
            this.enableMemCache = enableMemCache;
            return this;
        }

        public boolean isDebug() {
            return debug;
        }

        public boolean isEnableFileCache() {
            return enableFileCache;
        }

        public boolean isEnableMemCache() {
            return enableMemCache;
        }
    }

    class TaskLock {
        int taskId;

        TaskLock(int taskId) {
            this.taskId = taskId;
        }
    }

}
