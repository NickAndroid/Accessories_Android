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
import android.support.annotation.NonNull;

import dev.nick.imageloader.ui.animator.ViewAnimator;

public class ResViewSettings extends ViewSettings<Bitmap> {

    int mResId;

    public ResViewSettings(ViewAnimator<Bitmap> animator, @NonNull MediaHolder<Bitmap> mediaHolder, int resId) {
        super(animator, mediaHolder);
        this.mResId = resId;
    }

    protected void apply() {
        if (mAnimator != null) {
            mAnimator.animate(mSeat);
        }
        throw new UnsupportedOperationException();
    }
}