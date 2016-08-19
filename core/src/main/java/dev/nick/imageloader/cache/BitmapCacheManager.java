package dev.nick.imageloader.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.nick.imageloader.cache.disk.DiskCache;
import dev.nick.imageloader.cache.mem.MemCache;

public class BitmapCacheManager implements CacheManager<Bitmap> {

    private DiskCache mDiskCache;
    private Cache<String, Bitmap> mMemCache;

    private KeyGenerator mKeyGenerator;

    private ExecutorService mCacheService;

    private boolean isMemCacheEnabled;
    private boolean isDiskCacheEnabled;

    public BitmapCacheManager(CachePolicy cachePolicy, Context context) {
        isDiskCacheEnabled = cachePolicy.isDiskCacheEnabled();
        isMemCacheEnabled = cachePolicy.isMemCacheEnabled();
        mDiskCache = new DiskCache(cachePolicy, context);
        mMemCache = new MemCache(cachePolicy);
        mCacheService = Executors.newFixedThreadPool(cachePolicy.getCachingThreads());
        mKeyGenerator = cachePolicy.getKeyGenerator();
    }

    @Override
    public Bitmap get(String url) {
        return mMemCache.get(mKeyGenerator.fromUrl(url));
    }

    @Override
    public String getCachePath(String url) {
        return mDiskCache.getCachePath(mKeyGenerator.fromUrl(url));
    }

    @Override
    public boolean cache(@NonNull final String url, @NonNull final Bitmap value) {
        if (isMemCacheEnabled) {
            mMemCache.cache(url, value);
        } else if (isDiskCacheEnabled) {
            mCacheService.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    mDiskCache.cache(url, value);
                }
            });
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDiskCacheEnabled() {
        return isDiskCacheEnabled;
    }

    @Override
    public boolean isMemCacheEnabled() {
        return isMemCacheEnabled;
    }

    @WorkerThread
    public void evictDisk() {
        mDiskCache.evictAll();
    }

    @WorkerThread
    public void evictMem() {
        mMemCache.evictAll();
    }
}
