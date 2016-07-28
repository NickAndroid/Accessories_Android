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

package dev.nick.imageloader.display.transaction;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import dev.nick.imageloader.display.BitmapUtils;

public class BitmapTransaction {

    static final int FADE_IN_TIME = 500;

    private Drawable mFromDrawable;
    private Resources mResources;

    public BitmapTransaction(Resources resources) {
        this.mResources = resources;
        mFromDrawable = new ColorDrawable(Color.TRANSPARENT);
    }

    /**
     * Creates a transition drawable with default parameters
     *
     * @param bitmap the bitmap to transition to
     * @return the transition drawable
     */
    public TransitionDrawable createImageTransitionDrawable(final Bitmap bitmap) {
        return createImageTransitionDrawable(bitmap, FADE_IN_TIME, false, false);
    }

    /**
     * Creates a transition drawable
     *
     * @param bitmap   to transition to
     * @param fadeTime the time to fade in ms
     * @param dither   setting
     * @param force    force create a transition even if bitmap == null (fade to transparent)
     * @return the transition drawable
     */
    public TransitionDrawable createImageTransitionDrawable(final Bitmap bitmap,
                                                            final int fadeTime, final boolean dither, final boolean force) {
        return BitmapUtils.createImageTransitionDrawable(mResources, mFromDrawable, bitmap,
                fadeTime, dither, force);
    }
}
