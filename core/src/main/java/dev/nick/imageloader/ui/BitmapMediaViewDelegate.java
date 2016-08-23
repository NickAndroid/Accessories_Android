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
import android.view.animation.Animation;
import android.widget.ImageView;

import dev.nick.imageloader.utils.Preconditions;

/**
 * Wrapper class for a {@link ImageView}
 */
public class BitmapMediaViewDelegate implements MediaChair<Bitmap> {

    private ImageView mImageView;

    public BitmapMediaViewDelegate(ImageView imageView) {
        this.mImageView = Preconditions.checkNotNull(imageView);
    }

    @Override
    public void seat(@NonNull Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public int getWidth() {
        return mImageView.getWidth();
    }

    @Override
    public int getHeight() {
        return mImageView.getHeight();
    }

    @Override
    public void startAnimation(Animation animation) {
        mImageView.clearAnimation();
        mImageView.startAnimation(animation);
    }

    @Override
    public int hashCode() {
        return mImageView.hashCode();
    }
}
