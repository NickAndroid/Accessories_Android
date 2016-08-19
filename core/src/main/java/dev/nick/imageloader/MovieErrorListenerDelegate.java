package dev.nick.imageloader;

import android.graphics.Movie;

import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.result.ErrorListener;

class MovieErrorListenerDelegate extends ErrorListenerDelegate<Movie> {

    MovieErrorListenerDelegate(ErrorListener listener, Movie failureImg, ImageSeat<Movie> seat) {
        super(listener, failureImg, seat);
    }

    @Override
    void onApplyFailureImage(Movie image) {
        ImageSettingApplier.getSharedApplier().applyImageSettings(image, null, imageSeat, null);
    }
}
