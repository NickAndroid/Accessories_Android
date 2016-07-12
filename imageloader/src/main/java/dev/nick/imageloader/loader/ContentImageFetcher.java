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

import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.FailedCause;

public class ContentImageFetcher extends BaseImageFetcher {

    @NonNull
    ImageFetcher fileImageFetcher;

    public ContentImageFetcher(PathSplitter<String> splitter, @NonNull ImageFetcher fileImageFetcher) {
        super(splitter);
        this.fileImageFetcher = fileImageFetcher;
    }

    @Override
    public BitmapResult fetchFromUrl(@NonNull String url,
                                     DisplayOption.ImageQuality quality,
                                     ImageSpec info,
                                     ProgressListener listener) throws Exception {

        BitmapResult result = createEmptyResult();

        Uri uri = Uri.parse(url);

        String[] pro = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(uri, pro, null, null, null);

        if (cursor == null) {
            result.cause = FailedCause.CONTENT_NOT_EXISTS;
            return result;
        }

        if (cursor.getCount() == 0) {
            result.cause = FailedCause.CONTENT_NOT_EXISTS;
            return result;
        }

        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        if (index < 0) {
            result.cause = FailedCause.CONTENT_NOT_EXISTS;
            return result;
        }

        cursor.moveToFirst();

        String filePath = cursor.getString(index);

        cursor.close();

        return (BitmapResult) fileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + filePath,
                quality, info, listener);
    }
}
