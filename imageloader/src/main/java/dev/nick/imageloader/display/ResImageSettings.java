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

public class ResImageSettings implements Runnable {

    ImageAnimator animator;
    @NonNull
    ImageSettable settable;
    int resId;

    public ResImageSettings(ImageAnimator animator, int resId, @NonNull ImageSettable settable) {
        this.animator = animator;
        this.resId = resId;
        this.settable = settable;
    }

    void apply() {
        applyImageSetting(resId, settable, animator);
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