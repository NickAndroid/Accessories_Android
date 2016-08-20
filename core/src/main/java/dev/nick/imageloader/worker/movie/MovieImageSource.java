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

import android.graphics.Movie;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.imageloader.utils.Preconditions;
import dev.nick.imageloader.worker.ImageFetcher;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;

public class MovieImageSource extends ImageSource<Movie> {

    public static final MovieImageSource FILE = new FileSource();
    public static final MovieImageSource ASSETS = new AssetsSource();
    public static final MovieImageSource DRAWABLE = new DrawableSource();
    public static final MovieImageSource CONTENT = new ContentSource();
    public static final MovieImageSource HTTP = new HttpSource();
    public static final MovieImageSource HTTPS = new HttpsSource();

    private static final List<MovieImageSource> MOVIE_IMAGE_SOURCES = new ArrayList<>();

    static {
        addMovieSource(FILE);
        addMovieSource(ASSETS);
        addMovieSource(DRAWABLE);
        addMovieSource(CONTENT);
        addMovieSource(HTTP);
        addMovieSource(HTTPS);
    }

    public static void addMovieSource(@NonNull MovieImageSource source) {
        synchronized (MOVIE_IMAGE_SOURCES) {
            MOVIE_IMAGE_SOURCES.add(Preconditions.checkNotNull(source));
        }
    }

    public MovieImageSource(ImageFetcher<Movie> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    public static MovieImageSource from(String url) {
        synchronized (MOVIE_IMAGE_SOURCES) {
            for (MovieImageSource source : MOVIE_IMAGE_SOURCES) {
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

    static class FileSource extends MovieImageSource {

        public FileSource() {
            super(new FileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            }), Prefix.FILE);
        }
    }

    static class AssetsSource extends MovieImageSource {

        public AssetsSource() {
            super(new AssetsImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.ASSETS.length(), fullPath.length());
                }
            }), Prefix.ASSETS);
        }
    }

    static class DrawableSource extends MovieImageSource {

        public DrawableSource() {
            super(new DrawableImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.DRAWABLE.length(), fullPath.length());
                }
            }), Prefix.DRAWABLE);
        }
    }

    static class ContentSource extends MovieImageSource {
        public ContentSource() {
            super(new ContentImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }), Prefix.CONTENT);
        }
    }

    static class HttpSource extends MovieImageSource {

        public HttpSource() {
            super(new NetworkImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new HookedFileImageFetcher(new PathSplitter<String>() {
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

    static class HttpsSource extends MovieImageSource {

        public HttpsSource() {
            super(new NetworkImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new HookedFileImageFetcher(new PathSplitter<String>() {
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

    private static class HookedFileImageFetcher extends FileImageFetcher {

        public HookedFileImageFetcher(PathSplitter<String> splitter) {
            super(splitter);
        }

        @Override
        protected void callOnStart(ProgressListener<Movie> listener) {
            // Hooked, won't call.
        }
    }
}
