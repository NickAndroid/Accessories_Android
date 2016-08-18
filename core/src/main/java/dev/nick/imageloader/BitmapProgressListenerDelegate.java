package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.nick.imageloader.ui.BitmapImageSeat;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.task.DisplayTaskRecord;

public class BitmapProgressListenerDelegate extends ProgressListenerDelegate<Bitmap> {

    public BitmapProgressListenerDelegate(ProgressListener<Bitmap> listener,
                                          ViewSpec viewSpec,
                                          DisplayOption option,
                                          @NonNull ImageSeat<Bitmap> settable,
                                          DisplayTaskRecord taskRecord,
                                          String url) {
        super(listener, viewSpec, option, settable, taskRecord, url);
    }

    @Override
    public void onComplete(Bitmap result) {
        if (result == null) {
            return;
        }

        if (canceled) {
            cacheManager.cache(keyGenerator.fromUrl(url, viewSpec), result);
            return;
        }

        UIThreadRouter.getSharedRouter().callOnComplete(listener, result);

        final boolean isViewMaybeReused = option.isViewMaybeReused();

        if (!isViewMaybeReused || !checkTaskDirty()) {
            ImageAnimator animator = (option == null ? null : option.getAnimator());
            ImageArt[] handlers = (option == null ? null : option.getHandlers());
            ImageSettingApplier.getSharedApplier().applyImageSettings(result, handlers, settable, animator);
        }
        cacheManager.cache(keyGenerator.fromUrl(url, viewSpec), result);
    }
}
