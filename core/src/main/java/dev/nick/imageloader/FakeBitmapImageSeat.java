package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.animation.Animation;

import dev.nick.imageloader.ui.ImageSeat;

class FakeBitmapImageSeat implements ImageSeat<Bitmap> {

    String url;

    public FakeBitmapImageSeat(String url) {
        this.url = url;
    }

    @Override
    public void setImage(@NonNull Bitmap bitmap) {
        // Nothing.
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void startAnimation(Animation animation) {
        // Nothing.
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
