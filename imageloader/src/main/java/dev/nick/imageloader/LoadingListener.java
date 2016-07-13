package dev.nick.imageloader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.annotation.CallingOnUIThread;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public interface LoadingListener extends ProgressListener<BitmapResult>, ErrorListener {

    @Override
    @CallingOnUIThread
    void onError(@NonNull Cause cause);

    @Override
    @CallingOnUIThread
    void onComplete(@Nullable BitmapResult result);

    @Override
    @CallingOnUIThread
    void onProgressUpdate(float progress);

    @Override
    @CallingOnUIThread
    void onStartLoading();
}
