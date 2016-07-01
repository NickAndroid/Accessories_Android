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

package dev.nick.imageloader.display;

import android.support.annotation.DrawableRes;

import dev.nick.imageloader.display.animator.ImageAnimator;
import dev.nick.imageloader.display.processor.BitmapProcessor;

public class DisplayOption {

    private int defaultImgRes;
    private int loadingImgRes;

    private ImageQuality quality;

    private BitmapProcessor processor;
    private ImageAnimator animator;

    private DisplayOption(int defaultImgRes, int loadingImgRes, ImageQuality quality,
                          BitmapProcessor processor, ImageAnimator animator) {
        this.defaultImgRes = defaultImgRes;
        this.loadingImgRes = loadingImgRes;
        this.quality = quality;
        this.processor = processor;
        this.animator = animator;
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

    public BitmapProcessor getProcessor() {
        return processor;
    }

    public ImageAnimator getAnimator() {
        return animator;
    }

    public static class Builder {

        private int defaultImgRes;
        private int loadingImgRes;

        private ImageQuality quality = ImageQuality.FIT_VIEW;

        private BitmapProcessor processor;
        private ImageAnimator animator;

        /**
         * @param defaultImgRes Image res showing when load failure.
         * @return Instance of this builder.
         */
        public Builder defaultImgRes(@DrawableRes int defaultImgRes) {
            this.defaultImgRes = defaultImgRes;
            return this;
        }

        /**
         * @param loadingImgRes Image res showing when loading.
         * @return Instance of this builder.
         */
        public Builder loadingImgRes(@DrawableRes int loadingImgRes) {
            this.loadingImgRes = loadingImgRes;
            return this;
        }

        /**
         * @param processor {@link BitmapProcessor} instance using to process the bitmap before display.
         * @return Instance of this builder.
         */
        public Builder bitmapProcessor(BitmapProcessor processor) {
            this.processor = processor;
            return this;
        }

        public Builder imageAnimator(ImageAnimator animator) {
            this.animator = animator;
            return this;
        }

        public Builder imageQuality(ImageQuality quality) {
            this.quality = quality;
            return this;
        }

        public DisplayOption build() {
            return new DisplayOption(defaultImgRes, loadingImgRes, quality, processor, animator);
        }
    }

    public enum ImageQuality {
        /**
         * Using raw image when decode and display the image.
         */
        RAW,

        /**
         * Decrease the size of the image to fit the view dimen.
         */
        FIT_VIEW
    }
}
