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

import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.widget.ImageView;

import dev.nick.logger.LoggerManager;

/**
 * Wrapper class for a {@link ImageView}
 */
public class ImageViewDelegate implements ImageSettable {

    private ImageView imageView;

    public ImageViewDelegate(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        LoggerManager.getLogger(getClass()).verbose("setImageBitmap:" + bitmap + ", view:" + hashCode());
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void setImageResource(int resId) {
        imageView.setImageResource(resId);
    }

    @Override
    public int getWidth() {
        return imageView.getWidth();
    }

    @Override
    public int getHeight() {
        return imageView.getHeight();
    }

    @Override
    public void startAnimation(Animation animation) {
        imageView.clearAnimation();
        imageView.startAnimation(animation);
    }

    @Override
    public int hashCode() {
        return imageView.hashCode();
    }
}
