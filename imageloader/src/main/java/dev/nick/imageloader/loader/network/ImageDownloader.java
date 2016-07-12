package dev.nick.imageloader.loader.network;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.ErrorListener;

public interface ImageDownloader<T> {

    interface DownloadListener<T> extends ProgressListener, ErrorListener {
        void onDownloaded(T result);
    }

    void download(String url, DownloadListener<T> listener);
}
