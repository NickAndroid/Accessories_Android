package dev.nick.imageloader;

import android.graphics.Bitmap;

import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.result.ErrorListener;

class BitmapErrorListenerDelegate extends ErrorListenerDelegate<Bitmap> {

    BitmapErrorListenerDelegate(ErrorListener listener, Bitmap failureImg, ImageSeat<Bitmap> seat) {
        super(listener, failureImg, seat);
    }

    @Override
    void onApplyFailureImage(Bitmap image) {
        ImageSettingApplier.getSharedApplier().applyImageSettings(image, null, imageSeat, null);
    }
}
