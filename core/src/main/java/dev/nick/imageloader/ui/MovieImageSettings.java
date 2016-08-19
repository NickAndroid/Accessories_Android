package dev.nick.imageloader.ui;

import android.graphics.Movie;
import android.support.annotation.NonNull;

import dev.nick.imageloader.ui.animator.ImageAnimator;

public class MovieImageSettings extends ImageSettings<Movie> {

    Movie mMovie;

    public MovieImageSettings(ImageAnimator<Movie> animator, @NonNull ImageSeat<Movie> imageSeat, Movie movie) {
        super(animator, imageSeat);
        this.mMovie = movie;
    }

    @Override
    protected void apply() {
        if (mMovie != null) {
            mSeat.seat(mMovie);
            if (mAnimator != null) {
                mAnimator.animate(mSeat);
            }
        }
    }
}
