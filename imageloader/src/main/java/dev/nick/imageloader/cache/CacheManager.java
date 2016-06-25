package dev.nick.imageloader.cache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.nick.imageloader.cache.disk.DiskCache;
import dev.nick.imageloader.cache.mem.MemCache;

public class CacheManager implements Cache<String, Bitmap> {

    Cache<String, Bitmap> mDiskCache;
    Cache<String, Bitmap> mMemCache;

    public CacheManager() {
        mDiskCache = new DiskCache();
        mMemCache = new MemCache();
    }

    public CacheManager(Cache<String, Bitmap> mDiskCache, Cache<String, Bitmap> mMemCache) {
        this.mDiskCache = mDiskCache;
        this.mMemCache = mMemCache;
    }

    @Override
    public void cache(@NonNull String key, Bitmap value) {
        mMemCache.cache(key, value);
        mDiskCache.cache(key, value);
    }

    @Override
    public Bitmap get(@NonNull String key) {

        Bitmap out = mMemCache.get(key);

        if (out == null) {
            out = mDiskCache.get(key);
            if (out != null) mMemCache.cache(key, out);
        }

        return out;
    }
}
