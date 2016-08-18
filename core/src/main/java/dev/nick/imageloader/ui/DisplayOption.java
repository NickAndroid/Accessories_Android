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

package dev.nick.imageloader.ui;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import dev.nick.imageloader.ui.animator.ImageAnimator;
import dev.nick.imageloader.ui.art.ImageArt;
import dev.nick.imageloader.utils.Preconditions;

public class DisplayOption<T> {

    private int defaultImgRes;
    private int loadingImgRes;

    private ImageQuality quality;

    private ImageArt<T>[] handlers;
    private ImageAnimator<T> animator;

    private boolean viewMaybeReused;

    private boolean animateOnlyNewLoaded;

    private DisplayOption(int defaultImgRes,
                          int loadingImgRes,
                          ImageQuality quality,
                          ImageArt<T>[] handlers,
                          ImageAnimator<T> animator,
                          boolean viewMaybeReused,
                          boolean animateOnlyNewLoaded) {
        this.defaultImgRes = defaultImgRes;
        this.loadingImgRes = loadingImgRes;
        this.quality = quality;
        this.handlers = handlers;
        this.animator = animator;
        this.viewMaybeReused = viewMaybeReused;
        this.animateOnlyNewLoaded = animateOnlyNewLoaded;
    }

    public int getDefaultImgRes() {
        return defaultImgRes;
    }

    public int getLoadingImgRes() {
        return loadingImgRes;
    }

    public ImageQuality getQuality() {
        return quality;
    }

    public ImageArt<T>[] getHandlers() {
        return handlers;
    }

    public ImageAnimator<T> getAnimator() {
        return animator;
    }

    public boolean isViewMaybeReused() {
        return viewMaybeReused;
    }

    public boolean isAnimateOnlyNewLoaded() {
        return animateOnlyNewLoaded;
    }

    public static Builder<Bitmap> bitmapBuilder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private int defaultImgRes;
        private int loadingImgRes;

        private ImageQuality quality = ImageQuality.OPT;

        private ArrayList<ImageArt<T>> artList;
        private ImageAnimator<T> animator;

        private boolean viewMaybeReused;

        private boolean animateOnlyNewLoaded;

        private Builder() {
        }

        /**
         * @param defaultImgRes Image res showing when load failure.
         * @return Instance of this builder.
         */
        public Builder<T> showWithDefault(@DrawableRes int defaultImgRes) {
            this.defaultImgRes = defaultImgRes;
            return this;
        }

        /**
         * @param loadingImgRes Image res showing when loading.
         * @return Instance of this builder.
         */
        public Builder<T> showOnLoading(@DrawableRes int loadingImgRes) {
            this.loadingImgRes = loadingImgRes;
            return this;
        }

        /**
         * @param imageArt {@link ImageArt} instances using to process the bitmap before display.
         * @return Instance of this builder.
         */
        public synchronized Builder<T> imageArt(@NonNull ImageArt<T> imageArt) {
            if (artList == null) artList = new ArrayList<>();
            this.artList.add(Preconditions.checkNotNull(imageArt));
            return this;
        }

        /**
         * Set a image animator to perform an animation when displaying image.
         *
         * @param animator The animator you want to set.
         * @return @return Instance of this builder.
         */
        public Builder<T> imageAnimator(ImageAnimator<T> animator) {
            this.animator = animator;
            return this;
        }

        /**
         * Set the image quality displaying, lower image quality has
         * a better performance.
         *
         * @param quality Image quality when displaying.
         * @return Instance of this builder.
         */
        public Builder<T> imageQuality(ImageQuality quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Indicate that the view may be reused, the image won't be set if there is a new request for the same view.
         *
         * @return Instance of this builder.
         */
        public Builder<T> viewMaybeReused() {
            this.viewMaybeReused = true;
            return this;
        }

        /**
         * Indicate that only start an animation for new loaded images.
         *
         * @return Instance of this builder.
         */
        public Builder<T> animateOnlyNewLoaded() {
            this.animateOnlyNewLoaded = true;
            return this;
        }

        public DisplayOption<T> build() {
            //noinspection unchecked
            return new DisplayOption<>(
                    defaultImgRes,
                    loadingImgRes,
                    quality,
                    artList == null ? null : (ImageArt<T>[]) artList.toArray(),
                    animator,
                    viewMaybeReused,
                    animateOnlyNewLoaded);
        }
    }
}