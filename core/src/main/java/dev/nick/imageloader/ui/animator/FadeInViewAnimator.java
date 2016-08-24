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

package dev.nick.imageloader.ui.animator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import dev.nick.imageloader.ui.MediaHolder;

public class FadeInViewAnimator implements ViewAnimator<Bitmap> {
    @Override
    public void animate(@NonNull MediaHolder<Bitmap> settable) {
        AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
        fadeImage.setDuration(getDuration());
        fadeImage.setInterpolator(new DecelerateInterpolator());
        settable.startAnimation(fadeImage);
    }

    @Override
    public long getDuration() {
        return DEFAULT_DURATION;
    }
}
