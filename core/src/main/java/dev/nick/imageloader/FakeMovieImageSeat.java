package dev.nick.imageloader;

import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.view.animation.Animation;

import dev.nick.imageloader.ui.ImageSeat;

class FakeMovieImageSeat implements ImageSeat<Movie> {

    String url;

    public FakeMovieImageSeat(String url) {
        this.url = url;
    }

    @Override
    public void seat(@NonNull Movie movie) {
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
