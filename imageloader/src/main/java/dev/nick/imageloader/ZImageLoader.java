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
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.FadeInImageAnimator;
import dev.nick.imageloader.display.ImageAnimator;
import dev.nick.imageloader.display.ImageSettable;
import dev.nick.imageloader.display.ImageViewDelegate;
import dev.nick.imageloader.loader.ImageInfo;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.queue.MessageHandler;
import dev.nick.queue.MessageStackService;

public class ZImageLoader implements Handler.Callback {

    static final String LOG_TAG = "ZImageLoader";

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private ImageAnimator mDefaultImageAnimator;

    private Config mConfig;

    private MessageStackService<LoadTask> mStackService;

    private static ZImageLoader sLoader;

    private ZImageLoader(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        this.mCacheManager = new CacheManager(config, context);
        this.mStackService = MessageStackService.createStarted(
                new MessageHandler<LoadTask>() {
                    @Override
                    public boolean handleMessage(LoadTask task) {
                        task.run();
                        return true;
                    }
                });
    }

    public synchronized static void init(Context context, Config config) {
        if (sLoader == null) {
            sLoader = new ZImageLoader(context, config);
            return;
        }
        throw new IllegalArgumentException("Already configured.");
    }

    public static ZImageLoader getInstance() {
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
        displayImage(url, view, null, option);
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
            mCacheManager.get(url, new CacheManager.Callback() {
                @Override
                public void onResult(final Bitmap cached) {
                    if (cached != null) {
                        mUIThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                applyImageSetting(cached,
                                        viewDelegate,
                                        getDefaultAnimator());// Do not animate when loaded from cache?
                            }
                        });
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

        TaskCallback<Bitmap> callback = new ImageTaskCallback(new WeakReference<>(animator),
                option, url, new WeakReference<>(settable));

        mStackService.push(new LoadTask(callback, viewId, info, url));
    }

    private int createIdOfImageSettable(ImageSettable view) {
        return view.hashCode();
    }

    private void applyImageSetting(Bitmap bitmap, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            BitmapImageSettings settings = new BitmapImageSettings(animator, new WeakReference<Bitmap>(bitmap), settable);
            // mUIThreadHandler.obtainMessage(0, settings).sendToTarget();
            mUIThreadHandler.post(settings);
        }
    }

    private void applyImageSetting(int resId, ImageSettable settable, ImageAnimator animator) {
        if (settable != null) {
            ResImageSettings settings = new ResImageSettings(animator, resId, settable);
            // mUIThreadHandler.obtainMessage(0, settings).sendToTarget();
            mUIThreadHandler.post(settings);
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        Runnable settings = (Runnable) message.obj;
        settings.run();
        return true;
    }

    class BitmapImageSettings implements Runnable {

        ImageAnimator animator;
        @NonNull
        ImageSettable settable;
        WeakReference<Bitmap> bitmap;

        public BitmapImageSettings(ImageAnimator animator, WeakReference<Bitmap> bitmap, @NonNull ImageSettable settable) {
            this.animator = animator;
            this.bitmap = bitmap;
            this.settable = settable;
        }

        void apply() {
            applyImageSetting(bitmap.get(), settable, animator);
        }

        void applyImageSetting(Bitmap bitmap, ImageSettable settable, ImageAnimator animator) {
            if (bitmap != null) {
                settable.setImageBitmap(bitmap);
                if (animator != null) {
                    animator.animate(settable);
                }
            }
        }

        @Override
        public void run() {
            apply();
        }
    }

    class ResImageSettings implements Runnable {

        ImageAnimator animator;
        @NonNull
        ImageSettable settable;
        int resId;

        public ResImageSettings(ImageAnimator animator, int resId, @NonNull ImageSettable settable) {
            this.animator = animator;
            this.resId = resId;
            this.settable = settable;
        }

        void apply() {
            applyImageSetting(resId, settable, animator);
        }

        void applyImageSetting(int resId, ImageSettable settable, ImageAnimator animator) {
            settable.setImageResource(resId);
            if (animator != null) {
                animator.animate(settable);
            }
        }

        @Override
        public void run() {
            apply();
        }
    }

    class LoadTask implements Runnable, dev.nick.queue.Message {

        String url;
        ImageInfo info;
        TaskCallback<Bitmap> callback;

        int id;
        long upTime = System.currentTimeMillis();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoadTask loadTask = (LoadTask) o;

            if (id != loadTask.id) return false;
            if (!url.equals(loadTask.url)) return false;
            return info != null ? info.equals(loadTask.info) : loadTask.info == null;
        }

        @Override
        public int hashCode() {
            int result = url.hashCode();
            result = 31 * result + (info != null ? info.hashCode() : 0);
            result = 31 * result + id;
            return result;
        }

        @Override
        public String toString() {
            return "LoadTask{" +
                    "id=" + id +
                    ", upTime=" + upTime +
                    '}';
        }

        public LoadTask(TaskCallback<Bitmap> callback, int id, ImageInfo info, String url) {
            this.callback = callback;
            this.id = id;
            this.info = info;
            this.url = url;
        }

        @Override
        public void run() {

            if (mConfig.debug) Log.d(LOG_TAG, toString());

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            callback.onStart();

            ImageSource source = ImageSource.of(url);

            Bitmap bitmap;
            try {
                bitmap = source.getFetcher(mContext).fetchFromUrl(url, info);
                if (bitmap == null) {
                    callback.onError("No image got.");
                    return;
                }
                callback.onComplete(bitmap, isDirty());
            } catch (Exception e) {
                callback.onError("Error when fetch image:" + Log.getStackTraceString(e));
            }
        }

        boolean isDirty() {
            return false;
        }

    }

    interface TaskCallback<T> {
        void onStart();

        void onComplete(T result, boolean dirty);

        void onError(String errMsg);
    }

    class ImageTaskCallback implements TaskCallback<Bitmap> {

        WeakReference<ImageSettable> viewWeakReference;
        WeakReference<ImageAnimator> animatorWeakReference;
        String url;
        DisplayOption option;

        public ImageTaskCallback(WeakReference<ImageAnimator> animatorWeakReference,
                                 DisplayOption option, String url,
                                 WeakReference<ImageSettable> viewWeakReference) {
            this.animatorWeakReference = animatorWeakReference;
            this.option = option;
            this.url = url;
            this.viewWeakReference = viewWeakReference;
        }

        @Override
        public void onStart() {
            if (option != null) {
                int showWhenLoading = option.getImgResShowWhenLoading();
                if (showWhenLoading > 0)
                    applyImageSetting(showWhenLoading, viewWeakReference.get(), null);
            } else {
                applyImageSetting(null, viewWeakReference.get(), null);
            }
        }

        @Override
        public void onComplete(Bitmap result, boolean dirty) {
            if (result == null) return;
            applyImageSetting(result, viewWeakReference.get(), animatorWeakReference.get());
            if (!dirty) {

            } else if (mConfig.debug) {
                Log.d(LOG_TAG, "Skip settings for dirty image of url:" + url);
            }
            mCacheManager.cache(url, result);
        }

        @Override
        public void onError(String errMsg) {
            if (option != null) {
                int showWhenError = option.getImgResShowWhenError();
                if (showWhenError > 0)
                    applyImageSetting(showWhenError, viewWeakReference.get(), null);
            }
            if (mConfig.isDebug()) Log.e(LOG_TAG, errMsg);
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

}
