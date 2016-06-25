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
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.nick.imageloader.ZImageLoader;
import dev.nick.imageloader.cache.disk.DiskCache;
import dev.nick.imageloader.cache.mem.MemCache;

public class CacheManager {

    Cache<String, Bitmap> mDiskCache;
    Cache<String, Bitmap> mMemCache;

    private ZImageLoader.Config mConfig;

    private ExecutorService mCacheService;

    public CacheManager(ZImageLoader.Config config, Context context) {
        mDiskCache = new DiskCache(config, context);
        mMemCache = new MemCache();
        mConfig = config;
        mCacheService = Executors.newFixedThreadPool(config.getCacheThreads());

    }

    public CacheManager(Cache<String, Bitmap> mDiskCache, Cache<String, Bitmap> mMemCache) {
        this.mDiskCache = mDiskCache;
        this.mMemCache = mMemCache;
    }

    public void cache(@NonNull String key, Bitmap value) {
        internalCache(key, value);
    }

    private void internalCache(final String key, final Bitmap value) {
        if (mConfig.isEnableMemCache())
            mMemCache.cache(key, value);
        if (mConfig.isEnableFileCache())
            mCacheService.execute(new Runnable() {
                @Override
                public void run() {
                    mDiskCache.cache(key, value);
                }
            });
    }

    private Bitmap getAndCacheFromDisk(@NonNull String key) {

        Bitmap out = mDiskCache.get(key);
        if (out != null) mMemCache.cache(key, out);

        return out;
    }

    public void get(@NonNull final String key, @NonNull final Callback callback) {
        if (!mConfig.isEnableFileCache() && !mConfig.isEnableMemCache()) {
            callback.onResult(null);
            return;
        }

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
