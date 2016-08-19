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

package dev.nick.imageloader.worker.bitmap;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import dev.nick.imageloader.worker.BaseImageFetcher;
import dev.nick.imageloader.worker.DecodeSpec;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class AssetsImageFetcher extends BaseImageFetcher<Bitmap> {

    private AssetManager mAssets;

    public AssetsImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url,
                               @NonNull DecodeSpec decodeSpec,
                               @Nullable ProgressListener<Bitmap> progressListener,
                               @Nullable ErrorListener errorListener) throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        String path = mSplitter.getRealPath(url);

        if (mAssets == null) mAssets = mContext.getAssets();

        InputStream in;
        try {
            in = mAssets.open(path);
        } catch (IOException e) {
            callOnError(errorListener, new Cause(e));
            return null;
        }

        callOnStart(progressListener);

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        Rect rect = new Rect(0, 0, 0, 0);

        DimenSpec dimenSpec = decodeSpec.getDimenSpec();

        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, rect, decodeOptions);

        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = computeSampleSize(
                decodeOptions,
                UNCONSTRAINED,
                (dimenSpec.height * dimenSpec.height == 0 ?
                        MAX_NUM_PIXELS_THUMBNAIL
                        : dimenSpec.width * dimenSpec.height));

        Bitmap tempBitmap = null;

        try {
            tempBitmap = BitmapFactory.decodeStream(in, rect, decodeOptions);
        } catch (OutOfMemoryError error) {
            callOnError(errorListener, new Cause(error));
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        callOnComplete(progressListener, tempBitmap);
        return tempBitmap;
    }
}
