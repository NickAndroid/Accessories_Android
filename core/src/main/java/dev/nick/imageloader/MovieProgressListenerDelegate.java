package dev.nick.imageloader;

import android.graphics.Movie;

import java.util.ArrayList;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.task.DisplayTaskRecord;
import dev.nick.imageloader.worker.task.TaskManager;

class MovieProgressListenerDelegate extends ProgressListenerDelegate<Movie> {


    MovieProgressListenerDelegate(CacheManager<Movie> cacheManager,
                                  TaskManager taskManager,
                                  ProgressListener<Movie> listener,
                                  DimenSpec dimenSpec,
                                  DisplayOption<Movie> option,
                                  ImageSeat<Movie> imageSeat,
                                  DisplayTaskRecord taskRecord,
                                  String url) {
        super(cacheManager, taskManager, listener, dimenSpec, option, imageSeat, taskRecord, url);
    }

    @Override
    public void onComplete(Movie result) {
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
            ImageAnimator<Movie> animator = (option == null ? null : option.getAnimator());
            ArrayList<ImageArt<Movie>> handlers = (option == null ? null : option.getArtist());
            ImageSettingApplier.getSharedApplier().applyImageSettings(result, handlers, settable, animator);
        }
        cacheManager.cache(url, result);
    }
}
