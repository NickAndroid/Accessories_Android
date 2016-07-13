package dev.nick.imageloader.loader.network;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.ErrorListener;

public interface ImageDownloader<T> {
    T download(String url, ProgressListener progressListener, ErrorListener errorListener);
}
