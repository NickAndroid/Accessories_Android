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

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class ContentImageFetcher extends BaseImageFetcher {

    @NonNull
    ImageFetcher fileImageFetcher;

    public ContentImageFetcher(PathSplitter<String> splitter, @NonNull final ImageFetcher fileImageFetcher) {
        super(splitter);
        this.fileImageFetcher = fileImageFetcher;
    }

    @Override
    public void fetchFromUrl(@NonNull String url,
                             @NonNull DecodeSpec decodeSpec,
                             @Nullable ProgressListener<BitmapResult> progressListener,
                             @Nullable ErrorListener errorListener)
            throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        Uri uri = Uri.parse(url);

        String[] pro = {MediaStore.Images.Media.DATA};

        Cursor cursor = mContext.getContentResolver().query(uri, pro, null, null, null);

        if (cursor == null) {
            callOnError(errorListener, new Cause(new Exception(String.format("Cursor for %s is null.", url))));
            return;
        }

        try {
            if (cursor.getCount() == 0) {
                callOnError(errorListener, new Cause(new Exception(String.format("Cursor count for %s is 0.", url))));
                return;
            }

            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            if (index < 0) {
                callOnError(errorListener, new Cause(new Exception(String.format("Cursor index for %s is 0.", url))));
                return;
            }

            callOnStart(progressListener);

            cursor.moveToFirst();

            String filePath = cursor.getString(index);

            fileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + filePath,
                    decodeSpec, progressListener, errorListener);
        } finally {
            cursor.close();
        }
    }
}
