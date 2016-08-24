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

package dev.nick.accessories.worker.movie;

import android.content.res.AssetManager;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import dev.nick.accessories.worker.BaseMediaFetcher;
import dev.nick.accessories.worker.DecodeSpec;
import dev.nick.accessories.worker.PathSplitter;
import dev.nick.accessories.worker.ProgressListener;
import dev.nick.accessories.worker.result.Cause;
import dev.nick.accessories.worker.result.ErrorListener;
import lombok.Cleanup;

public class AssetsMediaFetcher extends BaseMediaFetcher<Movie> {

    AssetManager mAssets;

    public AssetsMediaFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Movie fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                              @Nullable ProgressListener<Movie> progressListener,
                              @Nullable ErrorListener errorListener) throws Exception {
        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        String path = mSplitter.getRealPath(url);

        synchronized (this) {
            if (mAssets == null) mAssets = mContext.getAssets();
        }

        @Cleanup
        InputStream in = null;
        try {
            in = mAssets.open(path);
            callOnStart(progressListener);
            Movie result = Movie.decodeStream(in);
            callOnComplete(progressListener, result);
            return result;
        } catch (IOException e) {
            callOnError(errorListener, new Cause(e));
            return null;
        }
    }
}
