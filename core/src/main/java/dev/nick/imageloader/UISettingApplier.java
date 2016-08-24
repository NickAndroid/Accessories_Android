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

package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;

import java.util.List;

import dev.nick.imageloader.annotation.Shared;
import dev.nick.imageloader.ui.BitmapViewSettings;
import dev.nick.imageloader.ui.MediaHolder;
import dev.nick.imageloader.ui.MovieViewSettings;
import dev.nick.imageloader.ui.ViewSettings;
import dev.nick.imageloader.ui.animator.ViewAnimator;
import dev.nick.imageloader.ui.art.MediaArt;
import dev.nick.imageloader.ui.art.MultipleMediaArtistCaller;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

class UISettingApplier implements Handler.Callback {

    private static final int MSG_APPLY_SETTINGS = 0x1;
    @Shared
    private static UISettingApplier sharedApplier;
    private Handler mUIThreadHandler;

    private Logger mLogger;

    private UISettingApplier() {
        mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
        mLogger = LoggerManager.getLogger(getClass());
    }

    public synchronized static UISettingApplier getSharedApplier() {
        if (sharedApplier == null) sharedApplier = new UISettingApplier();
        return sharedApplier;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_APPLY_SETTINGS:
                onApplyImageSettings((ViewSettings) message.obj);
                break;
        }
        return true;
    }

    private void onApplyImageSettings(ViewSettings settings) {
        mLogger.verbose(settings);
        settings.run();
    }

    @WorkerThread
    void applySettings(Bitmap bitmap, List<MediaArt<Bitmap>> arts, MediaHolder<Bitmap> mediaHolder,
                       ViewAnimator<Bitmap> animator) {

        mLogger.verbose("mediaHolder: " + mediaHolder + ", bitmap: " + bitmap + ", animator:" + animator);

        if (mediaHolder != null) {
            BitmapViewSettings settings = new BitmapViewSettings(
                    animator,
                    mediaHolder,
                    (arts == null || arts.size() == 0
                            ? bitmap
                            : MultipleMediaArtistCaller.call(arts, bitmap, mediaHolder)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_SETTINGS, settings).sendToTarget();
        }
    }

    void applySettings(Movie movie, List<MediaArt<Movie>> arts, MediaHolder<Movie> mediaHolder,
                       ViewAnimator<Movie> animator) {
        if (mediaHolder != null) {
            MovieViewSettings settings = new MovieViewSettings(
                    animator,
                    mediaHolder,
                    (arts == null || arts.size() == 0
                            ? movie
                            : MultipleMediaArtistCaller.call(arts, movie, mediaHolder)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_SETTINGS, settings).sendToTarget();
        }
    }
}
