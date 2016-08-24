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

package dev.nick.imageloader.worker.movie;

import android.content.res.Resources;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;

import dev.nick.imageloader.worker.BaseMediaFetcher;
import dev.nick.imageloader.worker.DecodeSpec;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class DrawableMediaFetcher extends BaseMediaFetcher<Movie> {

    public DrawableMediaFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Movie fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                              @Nullable ProgressListener<Movie> progressListener,
                              @Nullable ErrorListener errorListener) throws Exception {
        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        Resources resources = this.mContext.getResources();

        int resId = resources.getIdentifier(mSplitter.getRealPath(url),
                "drawable",
                this.mContext.getPackageName());

        if (resId <= 0) {
            callOnError(errorListener, new Cause(new Resources.NotFoundException(String.format("Res of id-%s not found.", resId))));
            return null;
        }

        callOnStart(progressListener);

        InputStream inputStream = null;
        try {
            inputStream = resources.openRawResource(0);
            Movie movie = Movie.decodeStream(inputStream);
            callOnComplete(progressListener, movie);
            return movie;
        } finally {
            if (inputStream != null) inputStream.close();
        }
    }
}
