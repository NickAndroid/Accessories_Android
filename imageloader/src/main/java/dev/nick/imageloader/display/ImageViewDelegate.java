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

import java.lang.ref.WeakReference;

public class ImageViewDelegate implements ImageSettable {

    WeakReference<ImageView> imageView;

    public ImageViewDelegate(WeakReference<ImageView> imageView) {
        this.imageView = imageView;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (imageView.get() != null) imageView.get().setImageBitmap(bitmap);
    }

    @Override
    public void setImageResource(int resId) {
        if (imageView.get() != null) imageView.get().setImageResource(resId);
    }

    @Override
    public int getWidth() {
        return imageView.get() == null ? 0 : imageView.get().getWidth();
    }

    @Override
    public int getHeight() {
        return imageView.get() == null ? 0 : imageView.get().getHeight();
    }

    @Override
    public void startAnimation(Animation animation) {
        if (imageView.get() != null) {
            imageView.get().clearAnimation();
            imageView.get().startAnimation(animation);
        }
    }

    @Override
    public int hashCode() {
        return imageView.get() == null ? 0 : imageView.get().hashCode();
    }
}
