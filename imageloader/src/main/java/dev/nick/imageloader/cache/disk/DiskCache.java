package dev.nick.imageloader.cache.disk;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.nick.imageloader.cache.Cache;

public class DiskCache implements Cache<String, Bitmap> {

    @Override
    public void cache(@NonNull String key, Bitmap value) {

    }

    @Override
    public Bitmap get(@NonNull String key) {
        return null;
    }
}
