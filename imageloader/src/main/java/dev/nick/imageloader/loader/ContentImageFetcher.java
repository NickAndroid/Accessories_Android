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
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import dev.nick.imageloader.display.DisplayOption;

public class ContentImageFetcher extends BaseImageFetcher {

    @NonNull
    ImageFetcher fileImageFetcher;

    public ContentImageFetcher(PathSplitter<String> splitter, @NonNull ImageFetcher fileImageFetcher) {
        super(splitter);
        this.fileImageFetcher = fileImageFetcher;
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, DisplayOption.ImageQuality quality, ImageSpec info) throws Exception {

        Uri uri = Uri.parse(url);

        String[] pro = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(uri, pro, null, null, null);

        if (cursor == null) {
            if (debug) logW("No cursor found for url:" + url);
            return null;
        }

        if (cursor.getCount() == 0) {
            if (debug) logW("Cursor count is ZERO for url:" + url);
            return null;
        }

        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String filePath = cursor.getString(index);

        cursor.close();

        return fileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + filePath, quality, info);
    }
}
