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

import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.FailedCause;

public class DrawableImageFetcher extends BaseImageFetcher {

    public DrawableImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public BitmapResult fetchFromUrl(@NonNull String url,
                                     DisplayOption.ImageQuality quality,
                                     ImageSpec spec,
                                     ProgressListener listener)
            throws Exception {

        BitmapResult result = createEmptyResult();

        Resources resources = this.context.getResources();

        int resId = resources.getIdentifier(splitter.getRealPath(url),
                "drawable",
                this.context.getPackageName());

        if (resId <= 0) {
            result.cause = FailedCause.RESOURCE_NOT_FOUND;
            return result;
        }

        BitmapFactory.Options decodeOptions = null;

        switch (quality) {
            case FIT_VIEW:
                decodeOptions = new BitmapFactory.Options();

                // If we have to resize this image, first get the natural bounds.
                decodeOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(resources, resId, decodeOptions);

                // Decode to the nearest power of two scaling factor.
                decodeOptions.inJustDecodeBounds = false;
                decodeOptions.inSampleSize =
                        computeSampleSize(decodeOptions, UNCONSTRAINED,
                                (spec.height * spec.height == 0 ?
                                        MAX_NUM_PIXELS_THUMBNAIL
                                        : spec.width * spec.height));
            default:
                break;
        }

        Bitmap tempBitmap = null;
        try {
            tempBitmap = BitmapFactory.decodeResource(resources, resId, decodeOptions);
        } catch (OutOfMemoryError error) {
            result.cause = FailedCause.OOM;
        }
        result.result = tempBitmap;
        return result;
    }
}
