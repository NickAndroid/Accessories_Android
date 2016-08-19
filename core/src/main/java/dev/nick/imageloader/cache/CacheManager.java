package dev.nick.imageloader.cache;

import android.support.annotation.NonNull;

public interface CacheManager<T> {
    T get(String url);

    String getCachePath(String url);

    boolean cache(@NonNull String url, @NonNull T value);

    boolean isDiskCacheEnabled();

    boolean isMemCacheEnabled();

    void evictDisk();

    void evictMem();
}
