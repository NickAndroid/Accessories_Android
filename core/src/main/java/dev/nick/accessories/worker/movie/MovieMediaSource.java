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

import android.graphics.Movie;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.utils.Preconditions;
import dev.nick.accessories.worker.MediaFetcher;
import dev.nick.accessories.worker.MediaSource;
import dev.nick.accessories.worker.PathSplitter;
import dev.nick.accessories.worker.ProgressListener;

public class MovieMediaSource extends MediaSource<Movie> {

    public static final MovieMediaSource FILE = new FileSource();
    public static final MovieMediaSource ASSETS = new AssetsSource();
    public static final MovieMediaSource DRAWABLE = new DrawableSource();
    public static final MovieMediaSource CONTENT = new ContentSource();
    public static final MovieMediaSource HTTP = new HttpSource();
    public static final MovieMediaSource HTTPS = new HttpsSource();

    private static final List<MovieMediaSource> MOVIE_IMAGE_SOURCES = new ArrayList<>();

    static {
        addMovieSource(FILE);
        addMovieSource(ASSETS);
        addMovieSource(DRAWABLE);
        addMovieSource(CONTENT);
        addMovieSource(HTTP);
        addMovieSource(HTTPS);
    }

    public static void addMovieSource(@NonNull MovieMediaSource source) {
        synchronized (MOVIE_IMAGE_SOURCES) {
            MOVIE_IMAGE_SOURCES.add(Preconditions.checkNotNull(source));
        }
    }

    public MovieMediaSource(MediaFetcher<Movie> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    public static MovieMediaSource from(String url) {
        synchronized (MOVIE_IMAGE_SOURCES) {
            for (MovieMediaSource source : MOVIE_IMAGE_SOURCES) {
                if (url.startsWith(source.getPrefix())) {
                    return source;
                }
            }
        }
        return null;
    }

    @Override
    public boolean maybeSlow() {
        return false;
    }

    static class FileSource extends MovieMediaSource {

        public FileSource() {
            super(new FileMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            }), Prefix.FILE);
        }
    }

    static class AssetsSource extends MovieMediaSource {

        public AssetsSource() {
            super(new AssetsMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.ASSETS.length(), fullPath.length());
                }
            }), Prefix.ASSETS);
        }
    }

    static class DrawableSource extends MovieMediaSource {

        public DrawableSource() {
            super(new DrawableMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.DRAWABLE.length(), fullPath.length());
                }
            }), Prefix.DRAWABLE);
        }
    }

    static class ContentSource extends MovieMediaSource {
        public ContentSource() {
            super(new ContentMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }), Prefix.CONTENT);
        }
    }

    static class HttpSource extends MovieMediaSource {

        public HttpSource() {
            super(new NetworkMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new HookedFileMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            })), Prefix.HTTP);
        }

        @Override
        public boolean maybeSlow() {
            return true;
        }
    }

    static class HttpsSource extends MovieMediaSource {

        public HttpsSource() {
            super(new NetworkMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new HookedFileMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            })), Prefix.HTTPS);
        }

        @Override
        public boolean maybeSlow() {
            return true;
        }
    }

    private static class HookedFileMediaFetcher extends FileMediaFetcher {

        public HookedFileMediaFetcher(PathSplitter<String> splitter) {
            super(splitter);
        }

        @Override
        protected void callOnStart(ProgressListener<Movie> listener) {
            // Hooked, won't call.
        }
    }
}
