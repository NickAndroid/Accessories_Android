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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public enum ImageSource {

    FILE(new FileImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(FILE.prefix.length(), fullPath.length());
        }
    }), "file://"),

    CONTENT(new ContentImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath;
        }
    }, new FileImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(FILE.prefix.length(), fullPath.length());
        }
    })) {
        @Override
        protected void callOnStart(ProgressListener<BitmapResult> listener) {
            // Ignored.
        }
    }, "content://"),

    ASSETS(new AssetsImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(ASSETS.prefix.length(), fullPath.length());
        }
    }), "assets://"),

    DRAWABLE(new DrawableImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(DRAWABLE.prefix.length(), fullPath.length());
        }
    }), "drawable://"),

    NETWORK_HTTP(new NetworkImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath;
        }
    }, new HookedFileImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(FILE.prefix.length(), fullPath.length());
        }
    })), "http://"),

    NETWORK_HTTPS(new NetworkImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath;
        }
    }, new HookedFileImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(FILE.prefix.length(), fullPath.length());
        }
    })), "https://"),

    UNKNOWN(new ImageFetcher() {
        @Override
        public void fetchFromUrl(@NonNull String url,
                                 @NonNull DecodeSpec decodeSpec,
                                 @Nullable ProgressListener progressListener,
                                 @Nullable ErrorListener errorListener)
                throws Exception {
            if (errorListener != null) {
                errorListener.onError(new Cause(new IllegalArgumentException("Unknown image source.")));
            }
        }

        @Override
        public ImageFetcher prepare(Context context, LoaderConfig config) {
            return this;
        }
    }, null);

    ImageFetcher fetcher;
    String prefix;

    ImageSource(ImageFetcher fetcher, String prefix) {
        this.fetcher = fetcher;
        this.prefix = prefix;
    }

    @NonNull
    public ImageFetcher getFetcher(Context context, LoaderConfig config) {
        return fetcher.prepare(context, config);

    }

    public String getPrefix() {
        return prefix;
    }

    public static ImageSource of(@NonNull String url) {
        for (ImageSource source : ImageSource.values()) {
            if (url.startsWith(source.prefix)) return source;
        }
        return ImageSource.UNKNOWN;
    }

    private static class HookedFileImageFetcher extends FileImageFetcher {

        public HookedFileImageFetcher(PathSplitter<String> splitter) {
            super(splitter);
        }

        @Override
        protected void callOnStart(ProgressListener<BitmapResult> listener) {
            // Hooked, won't call.
        }
    }
}
