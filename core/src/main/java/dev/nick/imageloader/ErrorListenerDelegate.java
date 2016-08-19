package dev.nick.imageloader;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.InterruptedIOException;

import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

abstract class ErrorListenerDelegate<T> implements ErrorListener {

    ErrorListener listener;

    T failureImg;

    ImageSeat<T> imageSeat;

    public ErrorListenerDelegate(ErrorListener listener, T failureImg, ImageSeat<T> seat) {
        this.listener = listener;
        this.failureImg = failureImg;
        this.imageSeat = seat;
    }

    @Override
    public void onError(@NonNull Cause cause) {
        if (LoggerManager.getDebugLevel() <= Log.ASSERT) {
            LoggerManager.getLogger(getClass()).warn(cause);
        }
        if (cause.exception instanceof InterruptedIOException) {
            // It's ok, We canceled this task.
        } else {
            if (failureImg != null) {
                onApplyFailureImage(failureImg);
            }
            UIThreadRouter.getSharedRouter().callOnFailure(listener, cause);
        }
    }

    abstract void onApplyFailureImage(T image);
}
