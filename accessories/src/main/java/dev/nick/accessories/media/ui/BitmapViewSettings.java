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

package dev.nick.accessories.media.ui;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.nick.accessories.media.ui.animator.ViewAnimator;
import lombok.ToString;

@ToString
public class BitmapViewSettings extends ViewSettings<Bitmap> {

    Bitmap mBitmap;

    public BitmapViewSettings(ViewAnimator<Bitmap> animator, @NonNull MediaHolder<Bitmap> settable, Bitmap bitmap) {
        super(animator, settable);
        this.mBitmap = bitmap;
    }

    @Override
    protected void apply() {
        mSeat.seat(mBitmap);
        if (mBitmap != null && mAnimator != null) {
            mAnimator.animate(mSeat);
        }
    }
}
