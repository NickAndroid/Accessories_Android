package dev.nick.accessories.media.loader.cache.mem;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import dev.nick.accessories.media.loader.cache.Cache;
import dev.nick.accessories.media.loader.cache.CachePolicy;

public class MemCache implements Cache<String, Bitmap> {

    private LruCache<String, Bitmap> mLruCache;

    public MemCache(CachePolicy cachePolicy) {
        int poolSize = cachePolicy.getMemCachePoolSize();
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
