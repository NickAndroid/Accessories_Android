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

package dev.nick.imageloader.display.handler;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.nick.imageloader.display.BitmapUtils;
import dev.nick.imageloader.display.ImageSettable;

public class RoundCornerBitmapHandler implements BitmapHandler {
    @NonNull
    @Override
    public Bitmap process(@NonNull Bitmap in, @NonNull ImageSettable settable) {
        return BitmapUtils.roundCorner(in, 24);
    }
}
