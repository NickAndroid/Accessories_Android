package dev.nick.imageloader;

import android.support.annotation.NonNull;

import java.io.InterruptedIOException;

import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

class ErrorListenerDelegate implements ErrorListener {

    ErrorListener listener;

    public ErrorListenerDelegate(ErrorListener listener) {
        this.listener = listener;
    }

    @Override
    public void onError(@NonNull Cause cause) {
        if (cause.exception instanceof InterruptedIOException) {
            // It's ok, We canceled this task.
        } else {
            UIThreadRouter.getSharedRouter().callOnFailure(listener, cause);
        }
    }
}
