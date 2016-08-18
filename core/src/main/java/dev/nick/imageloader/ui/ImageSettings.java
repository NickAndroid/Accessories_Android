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

import android.support.annotation.NonNull;

import dev.nick.imageloader.ui.animator.ImageAnimator;

public abstract class ImageSettings<T> implements Runnable {

    protected ImageAnimator<T> mAnimator;
    @NonNull
    protected ImageSeat<T> mSettable;

    public ImageSettings(ImageAnimator<T> animator,
                         @NonNull ImageSeat<T> imageSeat) {
        this.mAnimator = animator;
        this.mSettable = imageSeat;
    }

    protected abstract void apply();

    @Override
    public final void run() {
        apply();
    }
}
