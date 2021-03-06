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

package dev.nick.accessories.media.loader.ui;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import dev.nick.accessories.media.loader.ui.animator.ViewAnimator;
import dev.nick.accessories.media.loader.ui.art.MediaArt;
import dev.nick.accessories.media.loader.utils.Preconditions;
import lombok.Getter;

@Getter
public class DisplayOption<T> {

    private T failureImg;
    private T loadingImg;

    private boolean failureImgDefined;
    private boolean loadingImgDefined;

    private MediaQuality quality;

    private ArrayList<MediaArt<T>> mediaArts;
    private ViewAnimator<T> animator;

    private boolean viewMaybeReused;

    private boolean animateOnlyNewLoaded;

    private DisplayOption(T failureImg,
                          T loadingImg,
                          boolean failureImgDefined,
                          boolean loadingImgDefined,
                          MediaQuality quality,
                          ArrayList<MediaArt<T>> mediaArts,
                          ViewAnimator<T> animator,
                          boolean viewMaybeReused,
                          boolean animateOnlyNewLoaded) {
        this.failureImg = failureImg;
        this.loadingImg = loadingImg;
        this.loadingImgDefined = loadingImgDefined;
        this.failureImgDefined = failureImgDefined;
        this.quality = quality;
        this.mediaArts = mediaArts;
        this.animator = animator;
        this.viewMaybeReused = viewMaybeReused;
        this.animateOnlyNewLoaded = animateOnlyNewLoaded;
    }

    public static Builder<Bitmap> bitmapBuilder() {
        return new Builder<>();
    }

    public static Builder<Drawable> drawableBuilder() {
        return new Builder<>();
    }

    public static Builder<Movie> movieBuilder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private T failureImg;
        private T loadingImg;

        private boolean failureImgDefined;
        private boolean loadingImgDefined;

        private MediaQuality quality = MediaQuality.OPT;

        private ArrayList<MediaArt<T>> artList;
        private ViewAnimator<T> animator;

        private boolean viewMaybeReused;

        private boolean animateOnlyNewLoaded;

        private Builder() {
        }

        /**
         * @param failureImage Image res showing when load failure.
         * @return Instance of this builder.
         */
        public Builder<T> showOnFailure(@Nullable T failureImage) {
            this.failureImg = failureImage;
            this.failureImgDefined = true;
            return this;
        }

        /**
         * @param loadingImg Image res showing when loading.
         * @return Instance of this builder.
         */
        public Builder<T> showOnLoading(@Nullable T loadingImg) {
            this.loadingImg = loadingImg;
            this.loadingImgDefined = true;
            return this;
        }

        /**
         * @param mediaArt {@link MediaArt} instances using to process the bitmap before display.
         * @return Instance of this builder.
         */
        public synchronized Builder<T> imageArt(@NonNull MediaArt<T> mediaArt) {
            if (artList == null) artList = new ArrayList<>();
            this.artList.add(Preconditions.checkNotNull(mediaArt));
            return this;
        }

        /**
         * Set a image animator to perform an animation when displaying image.
         *
         * @param animator The animator you want to set.
         * @return @return Instance of this builder.
         */
        public Builder<T> imageAnimator(ViewAnimator<T> animator) {
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
        public Builder<T> imageQuality(MediaQuality quality) {
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
            return new DisplayOption<>(
                    failureImg,
                    loadingImg,
                    failureImgDefined,
                    loadingImgDefined,
                    quality,
                    artList,
                    animator,
                    viewMaybeReused,
                    animateOnlyNewLoaded);
        }
    }
}