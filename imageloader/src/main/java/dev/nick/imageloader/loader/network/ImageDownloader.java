package dev.nick.imageloader.loader.network;

import dev.nick.imageloader.loader.ProgressListener;

public interface ImageDownloader<T> {

    public interface DownloadListener<T> extends ProgressListener{
        void onDownloaded(T result);
    }

    void download(String url, DownloadListener<T> listener);
}
