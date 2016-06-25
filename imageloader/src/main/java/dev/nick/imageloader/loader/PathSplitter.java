package dev.nick.imageloader.loader;

import android.support.annotation.NonNull;

interface PathSplitter<T> {
    T getRealPath(@NonNull String fullPath);
}
