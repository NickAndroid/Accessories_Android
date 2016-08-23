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

package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.animation.Animation;

import dev.nick.imageloader.ui.MediaChair;

class FakeBitmapMediaChair implements MediaChair<Bitmap> {

    String url;

    public FakeBitmapMediaChair(String url) {
        this.url = url;
    }

    @Override
    public void seat(@NonNull Bitmap bitmap) {
        // Nothing.
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void startAnimation(Animation animation) {
        // Nothing.
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
