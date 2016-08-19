package dev.nick.imageloader;

import android.graphics.Bitmap;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.task.DisplayTaskRecord;
import dev.nick.imageloader.worker.task.TaskManager;

class BitmapProgressListenerDelegate extends ProgressListenerDelegate<Bitmap> {


    BitmapProgressListenerDelegate(CacheManager<Bitmap> cacheManager,
                                   TaskManager taskManager,
                                   ProgressListener<Bitmap> listener,
                                   DimenSpec dimenSpec,
                                   DisplayOption<Bitmap> option,
                                   ImageSeat<Bitmap> imageSeat,
                                   DisplayTaskRecord taskRecord,
                                   String url) {
        super(cacheManager, taskManager, listener, dimenSpec, option, imageSeat, taskRecord, url);
    }

    @Override
    public void onComplete(Bitmap result) {
        if (result == null) {
            return;
        }

        if (canceled) {
            cacheManager.cache(url, result);
            return;
        }

        UIThreadRouter.getSharedRouter().callOnComplete(listener, result);

        final boolean isViewMaybeReused = option.isViewMaybeReused();

        if (!isViewMaybeReused || !checkTaskDirty()) {
            ImageAnimator<Bitmap> animator = (option == null ? null : option.getAnimator());
            ImageArt<Bitmap>[] handlers = (option == null ? null : option.getHandlers());
            ImageSettingApplier.getSharedApplier().applyImageSettings(result, handlers, settable, animator);
        }
        cacheManager.cache(url, result);
    }
}
