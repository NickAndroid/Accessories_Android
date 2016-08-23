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

import dev.nick.imageloader.ui.MediaChair;
import dev.nick.imageloader.worker.result.ErrorListener;

class BitmapErrorListenerDelegate extends ErrorListenerDelegate<Bitmap> {

    BitmapErrorListenerDelegate(ErrorListener listener, Bitmap failureImg, MediaChair<Bitmap> seat) {
        super(listener, failureImg, seat);
    }

    @Override
    void onApplyFailureImage(Bitmap image) {
        UISettingApplier.getSharedApplier().applySettings(image, null, mediaChair, null);
    }
}
