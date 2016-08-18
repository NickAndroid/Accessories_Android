package dev.nick.imageloader;

import android.graphics.Bitmap;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.task.DisplayTaskRecord;
import dev.nick.imageloader.loader.task.TaskManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;

public class BitmapProgressListenerDelegate extends ProgressListenerDelegate<Bitmap> {


    public BitmapProgressListenerDelegate(CacheManager<Bitmap> cacheManager,
                                          TaskManager taskManager,
                                          ProgressListener<Bitmap> listener,
                                          ViewSpec viewSpec,
                                          DisplayOption<Bitmap> option,
                                          ImageSeat<Bitmap> imageSeat,
                                          DisplayTaskRecord taskRecord,
                                          String url) {
        super(cacheManager, taskManager, listener, viewSpec, option, imageSeat, taskRecord, url);
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
