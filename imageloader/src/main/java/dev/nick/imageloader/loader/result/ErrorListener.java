package dev.nick.imageloader.loader.result;

import android.support.annotation.NonNull;

public interface ErrorListener {
    void onError(@NonNull Cause cause);
}
