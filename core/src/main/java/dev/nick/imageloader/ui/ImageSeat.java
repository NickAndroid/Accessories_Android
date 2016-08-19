package dev.nick.imageloader.ui;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.animation.Animation;

public interface ImageSeat<T> {

    @UiThread
    void seat(@NonNull T image);

    int getWidth();

    int getHeight();

    @UiThread
    void startAnimation(Animation animation);
}
