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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class FileImageFetcher extends BaseImageFetcher {

    public FileImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public void fetchFromUrl(@NonNull String url,
                             @NonNull DecodeSpec decodeSpec,
                             @Nullable ProgressListener<BitmapResult> progressListener,
                             @Nullable ErrorListener errorListener) throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        String path = mSplitter.getRealPath(url);
        File file = new File(path);
        if (!file.exists()) {
            callOnError(errorListener, new Cause(new FileNotFoundException(String.format("File %s not found.", url))));
            return;
        }

        BitmapFactory.Options decodeOptions = null;
        ViewSpec viewSpec = decodeSpec.viewSpec;

        callOnStart(progressListener);

        switch (decodeSpec.quality) {
            case FIT_VIEW:
                decodeOptions = new BitmapFactory.Options();
                // If we have to resize this image, first get the natural bounds.
                decodeOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, decodeOptions);

                // Decode to the nearest power of two scaling factor.
                decodeOptions.inJustDecodeBounds = false;
                decodeOptions.inSampleSize =
                        computeSampleSize(decodeOptions, UNCONSTRAINED,
                                (viewSpec.height * viewSpec.height == 0 ?
                                        MAX_NUM_PIXELS_THUMBNAIL
                                        : viewSpec.width * viewSpec.height));
            default:
                break;
        }

        Bitmap tempBitmap;

        try {
            tempBitmap = BitmapFactory.decodeFile(path, decodeOptions);
        } catch (OutOfMemoryError error) {
            callOnError(errorListener, new Cause(error));
            return;
        }

        callOnComplete(progressListener, newResult(tempBitmap));
    }

    @Override
    protected void callOnStart(ProgressListener<BitmapResult> listener) {
        super.callOnStart(listener);
    }
}
