package dev.nick.imageloader.cache.mem;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.Cache;

public class MemCache implements Cache<String, Bitmap> {

    private LruCache<String, Bitmap> mLruCache;

    public MemCache(LoaderConfig config) {
        int poolSize = config.getCachePolicy().getMemCachePoolSize();
        mLruCache = new LruCache<String, Bitmap>(poolSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (value == null) return 0;
                return value.getWidth() * value.getHeight();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                // Hard to recycle.
                oldValue = null;
            }
        };
    }

    @Override
    public void cache(@NonNull String key, Bitmap value) {
        mLruCache.put(key, value);
    }

    @Override
    public Bitmap get(@NonNull String key) {
        return mLruCache.get(key);
    }

    @Override
    public void evictAll() {
        mLruCache.evictAll();
    }
}
