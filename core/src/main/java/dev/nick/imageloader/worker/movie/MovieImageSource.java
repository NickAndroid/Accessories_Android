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

import dev.nick.imageloader.worker.ImageFetcher;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.PathSplitter;

public class MovieImageSource extends ImageSource<Movie> {

    public static final MovieImageSource FILE = new FileSource();
    public static final MovieImageSource ASSETS = new AssetsSource();

    private static final MovieImageSource[] PREBUILT = new MovieImageSource[]{
            FILE, ASSETS
    };

    public MovieImageSource(ImageFetcher<Movie> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    public static MovieImageSource from(String url) {
        for (MovieImageSource source : PREBUILT) {
            if (url.startsWith(source.getPrefix())) {
                return source;
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
}
