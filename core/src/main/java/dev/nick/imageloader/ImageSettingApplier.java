package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;

import java.util.List;

import dev.nick.imageloader.annotation.Shared;
import dev.nick.imageloader.debug.Logger;
import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.ui.BitmapImageSettings;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.ImageSettings;
import dev.nick.imageloader.ui.MovieImageSettings;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.ui.art.ImageArtistCaller;

class ImageSettingApplier implements Handler.Callback {

    private static final int MSG_APPLY_IMAGE_SETTINGS = 0x1;
    @Shared
    private static ImageSettingApplier sharedApplier;
    private Handler mUIThreadHandler;

    private Logger mLogger;

    private ImageSettingApplier() {
        mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        mLogger = LoggerManager.getLogger(getClass());
    }

    public synchronized static ImageSettingApplier getSharedApplier() {
        if (sharedApplier == null) sharedApplier = new ImageSettingApplier();
        return sharedApplier;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_APPLY_IMAGE_SETTINGS:
                onApplyImageSettings((ImageSettings) message.obj);
                break;
        }
        return true;
    }

    private void onApplyImageSettings(ImageSettings settings) {
        mLogger.verbose(settings);
        settings.run();
    }

    @WorkerThread
    void applyImageSettings(Bitmap bitmap, List<ImageArt<Bitmap>> arts, ImageSeat<Bitmap> imageSeat,
                            ImageAnimator<Bitmap> animator) {

        mLogger.verbose("imageSeat: " + imageSeat + "bitmap: " + bitmap);

        if (imageSeat != null) {
            BitmapImageSettings settings = new BitmapImageSettings(
                    animator,
                    imageSeat,
                    (arts == null || arts.size() == 0
                            ? bitmap
                            : ImageArtistCaller.call(arts, bitmap, imageSeat)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    void applyImageSettings(Movie movie, List<ImageArt<Movie>> arts, ImageSeat<Movie> imageSeat,
                            ImageAnimator<Movie> animator) {
        if (imageSeat != null) {
            MovieImageSettings settings = new MovieImageSettings(
                    animator,
                    imageSeat,
                    (arts == null || arts.size() == 0
                            ? movie
                            : ImageArtistCaller.call(arts, movie, imageSeat)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }
}
