package dev.nick.imageloader.loader;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;

import dev.nick.imageloader.ui.BitmapImageSeat;
import dev.nick.imageloader.ui.BitmapImageSettings;
import dev.nick.imageloader.ui.ResImageSettings;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.ui.art.BitmapHandlerCaller;

class ImageSettingApplier implements Handler.Callback {

    private static final int MSG_APPLY_IMAGE_SETTINGS = 0x1;

    private Handler mUIThreadHandler;

    private static ImageSettingApplier sharedApplier;

    private ImageSettingApplier() {
        mUIThreadHandler = new Handler(Looper.getMainLooper());
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
    void applyImageSettings(Bitmap bitmap, ImageArt[] handlers, BitmapImageSeat settable,
                            ImageAnimator animator) {
        if (settable != null) {
            BitmapImageSettings settings = new BitmapImageSettings(
                    animator,
                    settable,
                    (handlers == null || handlers.length == 0
                            ? bitmap
                            : BitmapHandlerCaller.call(handlers, bitmap, settable)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    @WorkerThread
    void applyImageSettings(int resId, BitmapImageSeat settable, ImageAnimator animator) {
        if (settable != null) {
            ResImageSettings settings = new ResImageSettings(animator, settable, resId);
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }
}
