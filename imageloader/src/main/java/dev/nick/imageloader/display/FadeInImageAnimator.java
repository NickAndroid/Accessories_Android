package dev.nick.imageloader.display;

import android.support.annotation.NonNull;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class FadeInImageAnimator implements ImageAnimator {
    @Override
    public void animate(@NonNull ImageView imageView) {
        AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
        fadeImage.setDuration(DEFAULT_DURATION);
        fadeImage.setInterpolator(new DecelerateInterpolator());
        imageView.startAnimation(fadeImage);
    }
}
