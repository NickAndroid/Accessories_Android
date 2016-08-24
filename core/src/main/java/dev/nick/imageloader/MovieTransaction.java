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

package dev.nick.imageloader;

import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.MediaHolder;
import dev.nick.imageloader.ui.MediaQuality;
import dev.nick.imageloader.worker.MediaSource;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.movie.MovieMediaSource;
import dev.nick.imageloader.worker.result.ErrorListener;

public class MovieTransaction extends Transaction<Movie> {

    private static final DisplayOption<Movie> sDefDisplayOption = DisplayOption.movieBuilder()
            .imageQuality(MediaQuality.OPT)
            .viewMaybeReused()
            .build();

    MovieTransaction(@NonNull MediaLoader loader) {
        super(loader);
    }

    @Override
    public MovieTransaction from(@NonNull String url) {
        super.from(url);
        return this;
    }

    @Override
    MediaSource<Movie> onCreateSource(String url) {
        return MovieMediaSource.from(url);
    }

    @Override
    public MovieTransaction option(@NonNull DisplayOption<Movie> option) {
        super.option(option);
        return this;
    }

    @Override
    public MovieTransaction progressListener(@NonNull ProgressListener<Movie> listener) {
        super.progressListener(listener);
        return this;
    }

    @Override
    public MovieTransaction errorListener(@NonNull ErrorListener listener) {
        super.errorListener(listener);
        return this;
    }

    @Override
    public MovieTransaction priority(@NonNull Priority priority) {
        super.priority(priority);
        return this;
    }

    @Override
    public MovieTransaction into(@NonNull MediaHolder<Movie> settable) {
        super.into(settable);
        return this;
    }

    @Nullable
    @Override
    @LoaderApi
    public Movie startSynchronously() {
        try {
            return loader.displayMovie(
                    mediaData,
                    noneNullSettable(),
                    option.or(sDefDisplayOption),
                    progressListener,
                    errorListener,
                    priority)
                    .get();
        } catch (InterruptedException | ExecutionException | CancellationException ignored) {

        }
        return null;
    }

    @Override
    @LoaderApi
    void startAsync() {
        loader.displayMovie(
                mediaData,
                noneNullSettable(),
                option.or(sDefDisplayOption),
                progressListener,
                errorListener,
                priority);
    }


    protected MediaHolder<Movie> noneNullSettable() {
        return settable == null ? new FakeMovieMediaHolder(mediaData.getUrl()) : settable;
    }
}
