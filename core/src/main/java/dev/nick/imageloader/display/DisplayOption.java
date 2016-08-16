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

import dev.nick.imageloader.display.animator.ImageAnimator;
import dev.nick.imageloader.display.handler.BitmapHandler;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DisplayOption {

    private int showWithDefault;
    private int showOnLoading;

    private ImageQuality imageQuality;

    private BitmapHandler bitmapHandler;
    private ImageAnimator imageAnimator;

    private boolean applyImageOneByOne;
    private boolean viewMaybeReused;

    private boolean animateOnlyNewLoaded;

    private DisplayOption(int showWithDefault,
                          int showOnLoading,
                          ImageQuality imageQuality,
                          BitmapHandler bitmapHandler,
                          ImageAnimator imageAnimator,
                          boolean applyImageOneByOne,
                          boolean viewMaybeReused,
                          boolean animateOnlyNewLoaded) {
        this.showWithDefault = showWithDefault;
        this.showOnLoading = showOnLoading;
        this.imageQuality = imageQuality;
        this.bitmapHandler = bitmapHandler;
        this.imageAnimator = imageAnimator;
        this.applyImageOneByOne = applyImageOneByOne;
        this.viewMaybeReused = viewMaybeReused;
        this.animateOnlyNewLoaded = animateOnlyNewLoaded;
    }
}