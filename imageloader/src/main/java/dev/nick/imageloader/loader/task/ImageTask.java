package dev.nick.imageloader.loader.task;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.BitmapResult;

public interface ImageTask extends Task<ImageTaskRecord, Void> {
    @NonNull
    String getUrl();

    @Nullable ProgressListener<BitmapResult> getProgressListener();

    void setProgressListener(@Nullable ProgressListener<BitmapResult> listener);
}
