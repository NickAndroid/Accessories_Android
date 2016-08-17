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
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class ImageSourceType<X> {

    public static final ImageSourceType<Bitmap> FILE = new ImageSourceType<>(
            new FileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(FILE.prefix.length(), fullPath.length());
                }
            }), "file://");

    public static final ImageSourceType<Bitmap> CONTENT = new ImageSourceType<>
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
                protected void callOnStart(ProgressListener<Bitmap> listener) {
                    // Ignored.
                }
            }, "content://");

    public static final ImageSourceType<Bitmap> ASSETS = new ImageSourceType<>(
            new AssetsImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(ASSETS.prefix.length(), fullPath.length());
                }
            }), "assets://");

    public static final ImageSourceType<Bitmap> DRAWABLE = new ImageSourceType<>(
            new DrawableImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(DRAWABLE.prefix.length(), fullPath.length());
                }
            }), "drawable://");

    public static final ImageSourceType<Bitmap> NETWORK_HTTP = new ImageSourceType<>(
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

    public static final ImageSourceType<Bitmap> NETWORK_HTTPS = new ImageSourceType<>(
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

    public static final ImageSourceType<Bitmap> UNKNOWN = new ImageSourceType<>(
            new ImageFetcher<Bitmap>() {
                @Override
                public Bitmap fetchFromUrl(@NonNull String url,
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
                public ImageFetcher<Bitmap> prepare(Context context, LoaderConfig config) {
                    return this;
                }
            }, null);

    private static final ImageSourceType[] PREBUILT = new ImageSourceType[]{
            FILE, CONTENT, DRAWABLE, ASSETS, NETWORK_HTTP, NETWORK_HTTPS
    };

    private ImageFetcher<X> fetcher;
    private String prefix;

    public ImageSourceType(ImageFetcher<X> fetcher, String prefix) {
        this.fetcher = fetcher;
        this.prefix = prefix;
    }

    @NonNull
    public ImageFetcher<X> getFetcher(Context context, LoaderConfig config) {
        return fetcher.prepare(context, config);
    }

    @SuppressWarnings("unchecked")
    public static <T> ImageSourceType<T> of(@NonNull String url) {
        for (ImageSourceType source : ImageSourceType.values()) {
            if (url.startsWith(source.prefix)) return source;
        }
        return (ImageSourceType<T>) ImageSourceType.UNKNOWN;
    }

    public boolean isOneOf(@NonNull ImageSourceType... sources) {
        for (ImageSourceType source : sources) {
            if (source.equals(this)) return true;
        }
        return false;
    }

    public static ImageSourceType[] values() {
        return PREBUILT;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageSourceType<?> that = (ImageSourceType<?>) o;

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
        protected void callOnStart(ProgressListener<Bitmap> listener) {
            // Hooked, won't call.
        }
    }
}
