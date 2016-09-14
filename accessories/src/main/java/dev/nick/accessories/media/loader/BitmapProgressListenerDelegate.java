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

package dev.nick.accessories.media.loader;

import android.graphics.Bitmap;

import java.util.ArrayList;

import dev.nick.accessories.media.loader.cache.CacheManager;
import dev.nick.accessories.media.loader.ui.DisplayOption;
import dev.nick.accessories.media.loader.ui.MediaHolder;
import dev.nick.accessories.media.loader.ui.animator.ViewAnimator;
import dev.nick.accessories.media.loader.ui.art.MediaArt;
import dev.nick.accessories.media.loader.worker.DimenSpec;
import dev.nick.accessories.media.loader.worker.ProgressListener;
import dev.nick.accessories.media.loader.worker.task.DisplayTaskRecord;
import dev.nick.accessories.media.loader.worker.task.TaskManager;

class BitmapProgressListenerDelegate extends ProgressListenerDelegate<Bitmap> {


    BitmapProgressListenerDelegate(CacheManager<Bitmap> cacheManager,
                                   TaskManager taskManager,
                                   ProgressListener<Bitmap> listener,
                                   DimenSpec dimenSpec,
                                   DisplayOption<Bitmap> option,
                                   MediaHolder<Bitmap> mediaHolder,
                                   DisplayTaskRecord taskRecord,
                                   String url) {
        super(cacheManager, taskManager, listener, dimenSpec, option, mediaHolder, taskRecord, url);
    }

    @Override
    protected void callOnComplete(Bitmap result) {
        if (!canceled) {
            UIThreadRouter.getSharedRouter().callOnComplete(listener, result);
        }
    }

    @Override
    public void onComplete(Bitmap result) {

        callOnComplete(result);

        if (result == null) {
            mLogger.warn("onComplete call with null result");
            return;
        }

        if (canceled) {
            cacheManager.cache(url, result);
            mLogger.verbose("Skip calling back, canceled");
            return;
        }

        final boolean isViewMaybeReused = option.isViewMaybeReused();

        mLogger.verbose("isViewMaybeReused: " + isViewMaybeReused);

        if (!isViewMaybeReused || !checkTaskDirty()) {
            ViewAnimator<Bitmap> animator = (option == null ? null : option.getAnimator());
            ArrayList<MediaArt<Bitmap>> mediaArts = (option == null ? null : option.getMediaArts());
            UISettingApplier.getSharedApplier().applySettings(result, mediaArts, settable, animator);
        }
        cacheManager.cache(url, result);
    }
}
