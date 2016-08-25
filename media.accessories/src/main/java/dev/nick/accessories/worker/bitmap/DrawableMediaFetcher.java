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

package dev.nick.accessories.worker.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.accessories.worker.BaseMediaFetcher;
import dev.nick.accessories.worker.DecodeSpec;
import dev.nick.accessories.worker.DimenSpec;
import dev.nick.accessories.worker.PathSplitter;
import dev.nick.accessories.worker.ProgressListener;
import dev.nick.accessories.worker.result.Cause;
import dev.nick.accessories.worker.result.ErrorListener;

public class DrawableMediaFetcher extends BaseMediaFetcher<Bitmap> {

    public DrawableMediaFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url,
                               @NonNull DecodeSpec decodeSpec,
                               @Nullable ProgressListener<Bitmap> progressListener,
                               @Nullable ErrorListener errorListener)
            throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        Resources resources = this.mContext.getResources();

        int resId = resources.getIdentifier(mSplitter.getRealPath(url),
                "drawable",
                this.mContext.getPackageName());

        if (resId <= 0) {
            callOnError(errorListener, new Cause(new Resources.NotFoundException(String.format("Res of id-%s not found for url-%s.", resId, url))));
            return null;
        }

        callOnStart(progressListener);

        BitmapFactory.Options decodeOptions = null;
        DimenSpec dimenSpec = decodeSpec.getDimenSpec();

        switch (decodeSpec.getQuality()) {
            case OPT:
                decodeOptions = new BitmapFactory.Options();

                // If we have to resize this image, first get the natural bounds.
                decodeOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(resources, resId, decodeOptions);

                // Decode to the nearest power of two scaling factor.
                decodeOptions.inJustDecodeBounds = false;
                decodeOptions.inSampleSize =
                        computeSampleSize(decodeOptions, UNCONSTRAINED,
                                (dimenSpec.height * dimenSpec.height == 0 ?
                                        MAX_NUM_PIXELS_THUMBNAIL
                                        : dimenSpec.width * dimenSpec.height));
            default:
                break;
        }

        Bitmap bitmap;

        try {
            bitmap = BitmapFactory.decodeResource(resources, resId, decodeOptions);
        } catch (OutOfMemoryError error) {
            callOnError(errorListener, new Cause(error));
            return null;
        }

        callOnComplete(progressListener, bitmap);
        return bitmap;
    }
}
