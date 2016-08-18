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

package dev.nick.imageloader.worker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class ContentImageFetcher extends BaseImageFetcher<Bitmap> {

    @NonNull
    ImageFetcher<Bitmap> mFileImageFetcher;

    public ContentImageFetcher(PathSplitter<String> splitter, @NonNull final ImageFetcher<Bitmap> fileImageFetcher) {
        super(splitter);
        this.mFileImageFetcher = fileImageFetcher;
    }

    @Override
    public ImageFetcher<Bitmap> prepare(Context context, LoaderConfig config) {
        mFileImageFetcher.prepare(context, config);
        return super.prepare(context, config);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url,
                               @NonNull DecodeSpec decodeSpec,
                               @Nullable ProgressListener<Bitmap> progressListener,
                               @Nullable ErrorListener errorListener)
            throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        Uri uri = Uri.parse(url);

        String[] pro = {MediaStore.Images.Media.DATA};

        Cursor cursor = mContext.getContentResolver().query(uri, pro, null, null, null);

        if (cursor == null) {
            callOnError(errorListener, new Cause(new Exception(String.format("Cursor for %s is null.", url))));
            return null;
        }

        try {
            if (cursor.getCount() == 0) {
                callOnError(errorListener, new Cause(new Exception(String.format("Cursor count for %s is 0.", url))));
                return null;
            }

            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            if (index < 0) {
                callOnError(errorListener, new Cause(new Exception(String.format("Cursor index for %s is 0.", url))));
                return null;
            }

            callOnStart(progressListener);

            cursor.moveToFirst();

            String filePath = cursor.getString(index);

            return mFileImageFetcher.fetchFromUrl(ImageSourceType.FILE.getPrefix() + filePath,
                    decodeSpec, progressListener, errorListener);
        } finally {
            cursor.close();
        }
    }
}
