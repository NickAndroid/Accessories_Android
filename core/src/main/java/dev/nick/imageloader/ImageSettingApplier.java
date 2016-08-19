package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;

import dev.nick.imageloader.ui.BitmapImageSettings;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.MovieImageSettings;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.ui.art.ImageArtistCaller;

class ImageSettingApplier implements Handler.Callback {

    private static final int MSG_APPLY_IMAGE_SETTINGS = 0x1;

    private Handler mUIThreadHandler;

    private static ImageSettingApplier sharedApplier;

    private ImageSettingApplier() {
        mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
    }

    public synchronized static ImageSettingApplier getSharedApplier() {
        if (sharedApplier == null) sharedApplier = new ImageSettingApplier();
        return sharedApplier;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_APPLY_IMAGE_SETTINGS:
                onApplyImageSettings((Runnable) message.obj);
                break;
        }
        return true;
    }

    private void onApplyImageSettings(Runnable settings) {
        settings.run();
    }

    @WorkerThread
    void applyImageSettings(Bitmap bitmap, ImageArt<Bitmap>[] handlers, ImageSeat<Bitmap> imageSeat,
                            ImageAnimator<Bitmap> animator) {
        if (imageSeat != null) {
            BitmapImageSettings settings = new BitmapImageSettings(
                    animator,
                    imageSeat,
                    (handlers == null || handlers.length == 0
                            ? bitmap
                            : ImageArtistCaller.call(handlers, bitmap, imageSeat)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    void applyImageSettings(Movie movie, ImageArt<Movie>[] handlers, ImageSeat<Movie> imageSeat,
                            ImageAnimator<Movie> animator) {
        if (imageSeat != null) {
            MovieImageSettings settings = new MovieImageSettings(
                    animator,
                    imageSeat,
                    (handlers == null || handlers.length == 0
                            ? movie
                            : ImageArtistCaller.call(handlers, movie, imageSeat)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }
}
