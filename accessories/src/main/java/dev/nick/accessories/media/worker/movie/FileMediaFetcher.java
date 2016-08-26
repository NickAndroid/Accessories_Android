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

package dev.nick.accessories.media.worker.movie;

import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

import dev.nick.accessories.media.worker.BaseMediaFetcher;
import dev.nick.accessories.media.worker.DecodeSpec;
import dev.nick.accessories.media.worker.PathSplitter;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.result.Cause;
import dev.nick.accessories.media.worker.result.ErrorListener;

public class FileMediaFetcher extends BaseMediaFetcher<Movie> {

    public FileMediaFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Movie fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                              @Nullable ProgressListener<Movie> progressListener,
                              @Nullable ErrorListener errorListener) throws Exception {
        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        String path = mSplitter.getRealPath(url);

        File file = new File(path);
        if (!file.exists()) {
            callOnError(errorListener, new Cause(new FileNotFoundException(String.format("File %s not found.", url))));
            return null;
        }

        callOnStart(progressListener);

        Movie movie = Movie.decodeFile(path);
        callOnComplete(progressListener, movie);
        return movie;
    }
}
