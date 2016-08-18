package dev.nick.imageloader;

public interface CacheManager<T> {
    T get(String url);

    String getCachePath(String url);

    boolean cache(String url, T value);

    boolean isDiskCacheEnabled();

    boolean isMemCacheEnabled();

    void evictDisk();

    void evictMem();
}
