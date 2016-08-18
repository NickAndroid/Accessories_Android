package dev.nick.imageloader;

public interface CacheManager<T> {
    T get(String key);

    T getCachePath(String key);

    boolean cache(String key, T value);
}
