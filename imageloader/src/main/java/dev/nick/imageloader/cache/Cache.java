package dev.nick.imageloader.cache;

import android.support.annotation.NonNull;

public interface Cache<K, V> {
    void cache(@NonNull K key, V value);

    V get(@NonNull K key);
}
