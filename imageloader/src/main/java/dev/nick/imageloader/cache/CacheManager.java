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

package dev.nick.imageloader.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.cache.disk.DiskCache;
import dev.nick.imageloader.cache.mem.MemCache;
import dev.nick.imageloader.loader.ImageInfo;

public class CacheManager {

    Cache<String, Bitmap> mDiskCache;
    Cache<String, Bitmap> mMemCache;

    private KeyGenerator keyGenerator;

    private ImageLoader.Config mConfig;

    private ExecutorService mCacheService;

    public CacheManager(ImageLoader.Config config, Context context) {
        mDiskCache = new DiskCache(config, context);
        mMemCache = new MemCache();
        mConfig = config;
        mCacheService = Executors.newFixedThreadPool(config.getCacheThreads());
        keyGenerator = new HashcodeKeyGenerator();
    }

    public void cache(@NonNull String url, ImageInfo info, Bitmap value) {
        String key = keyGenerator.fromUrl(url, info);
        cacheByKey(key, value);
    }

    private void cacheByKey(final String key, final Bitmap value) {
        if (mConfig.isEnableMemCache())
            mMemCache.cache(key, value);
        if (mConfig.isEnableFileCache())
            mCacheService.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    mDiskCache.cache(key, value);
                }
            });
    }

    private Bitmap getAndCacheFromDisk(@NonNull String key) {

        Bitmap out = mDiskCache.get(key);
        if (out != null) mMemCache.cache(key, out);

        return out;
    }

    public void get(@NonNull final String url, @NonNull ImageInfo info, @NonNull final Callback callback) {
        if (!mConfig.isEnableFileCache() && !mConfig.isEnableMemCache()) {
            callback.onResult(null);
            return;
        }

        final String key = keyGenerator.fromUrl(url, info);

        Bitmap out = mMemCache.get(key);

        if (out != null) {
            callback.onResult(out);
            return;
        }

        mCacheService.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap out = getAndCacheFromDisk(key);
                callback.onResult(out);
            }
        });
    }

    public interface Callback {
        void onResult(Bitmap result);
    }
}
