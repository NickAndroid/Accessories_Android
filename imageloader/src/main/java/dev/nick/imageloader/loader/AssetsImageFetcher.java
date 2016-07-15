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

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class AssetsImageFetcher extends BaseImageFetcher {

    private AssetManager mAssets;

    public AssetsImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public void fetchFromUrl(@NonNull String url,
                             @NonNull DecodeSpec decodeSpec,
                             @Nullable ProgressListener<BitmapResult> progressListener,
                             @Nullable ErrorListener errorListener) throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        String path = mSplitter.getRealPath(url);

        if (mAssets == null) mAssets = mContext.getAssets();

        InputStream in;
        try {
            in = mAssets.open(path);
        } catch (IOException e) {
            callOnError(errorListener, new Cause(e));
            return;
        }

        callOnStart(progressListener);

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        Rect rect = new Rect(0, 0, 0, 0);

        ViewSpec viewSpec = decodeSpec.viewSpec;

        // If we have to resize this image, first get the natural bounds.
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, rect, decodeOptions);

        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = computeSampleSize(
                decodeOptions,
                UNCONSTRAINED,
                (viewSpec.height * viewSpec.height == 0 ?
                        MAX_NUM_PIXELS_THUMBNAIL
                        : viewSpec.width * viewSpec.height));

        Bitmap tempBitmap = null;

        try {
            tempBitmap = BitmapFactory.decodeStream(in, rect, decodeOptions);
        } catch (OutOfMemoryError error) {
            callOnError(errorListener, new Cause(error));
            return;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        callOnComplete(progressListener, newResult(tempBitmap));
    }
}
