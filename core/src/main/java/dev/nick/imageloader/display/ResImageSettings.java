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

import android.support.annotation.NonNull;

import dev.nick.imageloader.display.animator.ImageAnimator;

public class ResImageSettings implements Runnable {

    ImageAnimator mAnimator;
    @NonNull
    ImageSettable mSettable;
    int mResId;

    public ResImageSettings(ImageAnimator animator, int resId, @NonNull ImageSettable settable) {
        this.mAnimator = animator;
        this.mResId = resId;
        this.mSettable = settable;
    }

    void apply() {
        applyImageSetting(mResId, mSettable, mAnimator);
    }

    void applyImageSetting(int resId, ImageSettable settable, ImageAnimator animator) {
        settable.setImageResource(resId);
        if (animator != null) {
            animator.animate(settable);
        }
    }

    @Override
    public void run() {
        apply();
    }
}