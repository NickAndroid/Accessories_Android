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
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.FadeInImageAnimator;
import dev.nick.imageloader.display.ImageAnimator;
import dev.nick.imageloader.loader.ImageInfo;
import dev.nick.imageloader.loader.ImageSource;

public class ZImageLoader {

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private ImageAnimator mDefaultImageAnimator;

    private ExecutorService mLoaderService;

    private Config mConfig;

    private static ZImageLoader sLoader;

    private ZImageLoader(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUIThreadHandler = new Handler(Looper.getMainLooper());
        this.mCacheManager = new CacheManager(config, context);
        this.mLoaderService = Executors.newFixedThreadPool(mConfig.loadingThreads);
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

    public ImageAnimator getDefaultAnimator() {
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
        mCacheManager.get(url, new CacheManager.Callback() {
            @Override
            public void onResult(final Bitmap cached) {
                if (cached != null) {
                    mUIThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            applyImageSetting(cached, view, getDefaultAnimator());// Do not animate when loaded from cache?
                        }
                    });
                } else {
                    displayImageAfterLoaded(url, view, animator, option);
                }
            }
        });
    }

    private void displayImageAfterLoaded(final String url,
                                         final ImageView view,
                                         final ImageAnimator animator,
                                         final DisplayOption option) {

        final int imgResWhenLoading = option == null ? 0 : option.getImgResShowWhenLoading();
        final int imgResWhenError = option == null ? 0 : option.getImgResShowWhenError();

        mLoaderService.execute(new LoadTask(url, new ImageInfo(view.getWidth(), view.getHeight()),
                new TaskCallback<Bitmap>() {
                    @Override
                    public void onComplete(Bitmap result) {
                        if (result == null) return;
                        applyImageSetting(result, view, animator);
                        mCacheManager.cache(url, result);
                    }

                    @Override
                    public void onStart() {
                        if (imgResWhenLoading > 0) {
                            applyImageSetting(imgResWhenLoading, view, null);
                        }
                    }

                    @Override
                    public void onError(String errMsg) {
                        if (imgResWhenError > 0) {
                            applyImageSetting(imgResWhenError, view, null);
                        }
                        if (mConfig.isDebug()) Log.e("ZImageLoader", errMsg);
                    }
                }));
    }

    private void applyImageSetting(Bitmap bitmap, ImageView imageView, ImageAnimator animator) {
        imageView.setImageBitmap(bitmap);
        if (animator != null)
            animator.animate(imageView);
    }

    private void applyImageSetting(int resId, ImageView imageView, ImageAnimator animator) {
        imageView.setImageResource(resId);
        if (animator != null)
            animator.animate(imageView);
    }

    class LoadTask implements Runnable {

        String url;
        ImageInfo info;
        TaskCallback<Bitmap> callback;

        public LoadTask(String url, ImageInfo info, TaskCallback<Bitmap> callback) {
            this.url = url;
            this.info = info;
            this.callback = callback;
        }

        @Override
        public void run() {
            Runnable startRunnable = new Runnable() {
                @Override
                public void run() {
                    callback.onStart();
                }
            };
            mUIThreadHandler.post(startRunnable);
            ImageSource source = ImageSource.of(url);
            final Bitmap bitmap;
            try {
                bitmap = source.getFetcher(mContext).fetchFromUrl(url, info);
                if (bitmap == null) {
                    callOnError("Unknown error.");
                    return;
                }
                Runnable finishRunnable = new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(bitmap);
                    }
                };
                mUIThreadHandler.post(finishRunnable);
            } catch (Exception e) {
                e.printStackTrace();
                callOnError("Error when fetch image:" + Log.getStackTraceString(e));
            }
        }

        void callOnError(final String errMsg) {
            Runnable errorRunnable = new Runnable() {
                @Override
                public void run() {
                    callback.onError(errMsg);
                }
            };
            mUIThreadHandler.post(errorRunnable);
        }
    }

    interface TaskCallback<T> {
        void onStart();

        void onComplete(T result);

        void onError(String errMsg);
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
