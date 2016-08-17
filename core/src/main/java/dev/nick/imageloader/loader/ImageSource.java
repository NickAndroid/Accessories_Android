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
import dev.nick.imageloader.loader.result.Result;

public class ImageSource<X> {

    public static final ImageSource<BitmapResult> FILE = new ImageSource<>(
            new FileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(FILE.prefix.length(), fullPath.length());
                }
            }), "file://");

    public static final ImageSource<BitmapResult> CONTENT = new ImageSource<>
            (new ContentImageFetcher(new PathSplitter<String>() {
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
            }, "content://");

    public static final ImageSource<BitmapResult> ASSETS = new ImageSource<>(
            new AssetsImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(ASSETS.prefix.length(), fullPath.length());
                }
            }), "assets://");

    public static final ImageSource<BitmapResult> DRAWABLE = new ImageSource<>(
            new DrawableImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(DRAWABLE.prefix.length(), fullPath.length());
                }
            }), "drawable://");

    public static final ImageSource<BitmapResult> NETWORK_HTTP = new ImageSource<>(
            new NetworkImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new HookedFileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(FILE.prefix.length(), fullPath.length());
                }
            })), "http://");

    public static final ImageSource<BitmapResult> NETWORK_HTTPS = new ImageSource<>(
            new NetworkImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new HookedFileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(FILE.prefix.length(), fullPath.length());
                }
            })), "https://");

    public static final ImageSource<BitmapResult> UNKNOWN = new ImageSource<>(
            new ImageFetcher<BitmapResult>() {
                @Override
                public BitmapResult fetchFromUrl(@NonNull String url,
                                                 @NonNull DecodeSpec decodeSpec,
                                                 @Nullable ProgressListener progressListener,
                                                 @Nullable ErrorListener errorListener)
                        throws Exception {
                    if (errorListener != null) {
                        errorListener.onError(new Cause(new IllegalArgumentException("Unknown image source called.")));
                    }
                    return null;
                }

                @Override
                public ImageFetcher<BitmapResult> prepare(Context context, LoaderConfig config) {
                    return this;
                }
            }, null);

    private static final ImageSource[] PREBUILT = new ImageSource[]{
            FILE, CONTENT, DRAWABLE, ASSETS, NETWORK_HTTP, NETWORK_HTTPS
    };

    private ImageFetcher<X> fetcher;
    private String prefix;

    public ImageSource(ImageFetcher<X> fetcher, String prefix) {
        this.fetcher = fetcher;
        this.prefix = prefix;
    }

    @NonNull
    public ImageFetcher<X> getFetcher(Context context, LoaderConfig config) {
        return fetcher.prepare(context, config);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Result> ImageSource<T> of(@NonNull String url) {
        for (ImageSource source : ImageSource.values()) {
            if (url.startsWith(source.prefix)) return source;
        }
        return (ImageSource<T>) ImageSource.UNKNOWN;
    }

    public boolean isOneOf(@NonNull ImageSource... sources) {
        for (ImageSource source : sources) {
            if (source.equals(this)) return true;
        }
        return false;
    }

    public static ImageSource[] values() {
        return PREBUILT;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageSource<?> that = (ImageSource<?>) o;

        return prefix != null ? prefix.equals(that.prefix) : that.prefix == null;
    }

    @Override
    public int hashCode() {
        return prefix != null ? prefix.hashCode() : 0;
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
