/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.accessories.media;

import android.graphics.Movie;

import java.util.ArrayList;

import dev.nick.accessories.media.cache.CacheManager;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.MediaHolder;
import dev.nick.accessories.media.ui.animator.ViewAnimator;
import dev.nick.accessories.media.ui.art.MediaArt;
import dev.nick.accessories.media.worker.DimenSpec;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.task.DisplayTaskRecord;
import dev.nick.accessories.media.worker.task.TaskManager;

class MovieProgressListenerDelegate extends ProgressListenerDelegate<Movie> {


    MovieProgressListenerDelegate(CacheManager<Movie> cacheManager,
                                  TaskManager taskManager,
                                  ProgressListener<Movie> listener,
                                  DimenSpec dimenSpec,
                                  DisplayOption<Movie> option,
                                  MediaHolder<Movie> mediaHolder,
                                  DisplayTaskRecord taskRecord,
                                  String url) {
        super(cacheManager, taskManager, listener, dimenSpec, option, mediaHolder, taskRecord, url);
    }

    @Override
    protected void callOnComplete(Movie result) {
        if (!canceled) {
            UIThreadRouter.getSharedRouter().callOnComplete(listener, result);
        }
    }

    @Override
    public void onComplete(Movie result) {

        callOnComplete(result);

        if (result == null) {
            return;
        }

        if (canceled) {
            cacheManager.cache(url, result);
            return;
        }

        final boolean isViewMaybeReused = option.isViewMaybeReused();

        if (!isViewMaybeReused || !checkTaskDirty()) {
            ViewAnimator<Movie> animator = (option == null ? null : option.getAnimator());
            ArrayList<MediaArt<Movie>> handlers = (option == null ? null : option.getMediaArts());
            UISettingApplier.getSharedApplier().applySettings(result, handlers, settable, animator);
        }
        cacheManager.cache(url, result);
    }
}
