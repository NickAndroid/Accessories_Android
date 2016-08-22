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
import dev.nick.imageloader.ui.BitmapImageSettings;
import dev.nick.imageloader.ui.ImageChair;
import dev.nick.imageloader.ui.ImageSettings;
import dev.nick.imageloader.ui.MovieImageSettings;
import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.ui.art.ImageArtistCaller;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

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
    void applyImageSettings(Bitmap bitmap, List<ImageArt<Bitmap>> arts, ImageChair<Bitmap> imageChair,
                            ImageAnimator<Bitmap> animator) {

        mLogger.verbose("imageChair: " + imageChair + ", bitmap: " + bitmap + ", animator:" + animator);

        if (imageChair != null) {
            BitmapImageSettings settings = new BitmapImageSettings(
                    animator,
                    imageChair,
                    (arts == null || arts.size() == 0
                            ? bitmap
                            : ImageArtistCaller.call(arts, bitmap, imageChair)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }

    void applyImageSettings(Movie movie, List<ImageArt<Movie>> arts, ImageChair<Movie> imageChair,
                            ImageAnimator<Movie> animator) {
        if (imageChair != null) {
            MovieImageSettings settings = new MovieImageSettings(
                    animator,
                    imageChair,
                    (arts == null || arts.size() == 0
                            ? movie
                            : ImageArtistCaller.call(arts, movie, imageChair)));
            mUIThreadHandler.obtainMessage(MSG_APPLY_IMAGE_SETTINGS, settings).sendToTarget();
        }
    }
}
