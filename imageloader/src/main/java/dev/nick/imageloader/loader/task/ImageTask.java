package dev.nick.imageloader.loader.task;

import android.support.annotation.NonNull;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.BitmapResult;

public interface ImageTask extends Task<ImageTaskRecord, Void> {
    @NonNull
    String getUrl();

    ProgressListener<BitmapResult> getProgressListener();
}
