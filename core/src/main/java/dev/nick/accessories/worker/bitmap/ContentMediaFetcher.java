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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.accessories.AccessoryConfig;
import dev.nick.accessories.worker.BaseMediaFetcher;
import dev.nick.accessories.worker.DecodeSpec;
import dev.nick.accessories.worker.MediaFetcher;
import dev.nick.accessories.worker.MediaSource;
import dev.nick.accessories.worker.PathSplitter;
import dev.nick.accessories.worker.ProgressListener;
import dev.nick.accessories.worker.result.Cause;
import dev.nick.accessories.worker.result.ErrorListener;

public class ContentMediaFetcher extends BaseMediaFetcher<Bitmap> {

    @NonNull
    MediaFetcher<Bitmap> mFileMediaFetcher;

    public ContentMediaFetcher(PathSplitter<String> splitter, @NonNull final MediaFetcher<Bitmap> fileMediaFetcher) {
        super(splitter);
        this.mFileMediaFetcher = fileMediaFetcher;
    }

    @Override
    public MediaFetcher<Bitmap> prepare(Context context, AccessoryConfig config) {
        mFileMediaFetcher.prepare(context, config);
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

            return mFileMediaFetcher.fetchFromUrl(MediaSource.Prefix.FILE + filePath,
                    decodeSpec, progressListener, errorListener);
        } finally {
            cursor.close();
        }
    }
}
