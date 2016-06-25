package dev.nick.imageloader.display;

import android.support.annotation.NonNull;
import android.widget.ImageView;

public interface ImageAnimator {

    long DEFAULT_DURATION = 500;

    void animate(@NonNull ImageView imageView);
}
