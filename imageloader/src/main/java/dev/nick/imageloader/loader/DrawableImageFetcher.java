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

package dev.nick.imageloader.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

public class DrawableImageFetcher extends BaseImageFetcher {

    public DrawableImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception {

        Resources resources = this.context.getResources();

        int resId = resources.getIdentifier(splitter.getRealPath(url),
                "drawable",
                this.context.getPackageName());

        if (resId <= 0) throw new Resources.NotFoundException("Res:" + url);

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, decodeOptions);

        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize =
                computeSampleSize(decodeOptions, UNCONSTRAINED,
                        (info.height * info.height == 0 ?
                                MAX_NUM_PIXELS_THUMBNAIL
                                : info.width * info.height));
        Bitmap tempBitmap;
        try {
            tempBitmap = BitmapFactory.decodeResource(resources, resId, decodeOptions);
        } catch (OutOfMemoryError error) {
            throw new RuntimeException("OutOfMemoryError:" + Log.getStackTraceString(error));
        }
        return tempBitmap;
    }
}
