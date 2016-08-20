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

package dev.nick.imageloader.worker.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.imageloader.utils.Preconditions;
import dev.nick.imageloader.worker.ImageFetcher;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;

public class BitmapImageSource extends ImageSource<Bitmap> {

    public static final BitmapImageSource FILE = new FileSource();
    public static final BitmapImageSource ASSETS = new AssetsSource();
    public static final BitmapImageSource DRAWABLE = new DrawableSource();
    public static final BitmapImageSource CONTENT = new ContentSource();
    public static final BitmapImageSource HTTP = new HttpSource();
    public static final BitmapImageSource HTTPS = new HttpsSource();

    private static final List<BitmapImageSource> BITMAP_IMAGE_SOURCES = new ArrayList<>();

    public BitmapImageSource(ImageFetcher<Bitmap> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    static {
        addBitmapSource(FILE);
        addBitmapSource(ASSETS);
        addBitmapSource(DRAWABLE);
        addBitmapSource(CONTENT);
        addBitmapSource(HTTP);
        addBitmapSource(HTTPS);
    }

    public static void addBitmapSource(@NonNull BitmapImageSource source) {
        synchronized (BITMAP_IMAGE_SOURCES) {
            BITMAP_IMAGE_SOURCES.add(Preconditions.checkNotNull(source));
        }
    }

    public static BitmapImageSource from(String url) {
        synchronized (BITMAP_IMAGE_SOURCES) {
            for (BitmapImageSource source : BITMAP_IMAGE_SOURCES) {
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

    static class FileSource extends BitmapImageSource {

        public FileSource() {
            super(new FileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            }), Prefix.FILE);
        }
    }

    static class AssetsSource extends BitmapImageSource {

        public AssetsSource() {
            super(new AssetsImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.ASSETS.length(), fullPath.length());
                }
            }), Prefix.ASSETS);
        }
    }

    static class DrawableSource extends BitmapImageSource {

        public DrawableSource() {
            super(new DrawableImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.DRAWABLE.length(), fullPath.length());
                }
            }), Prefix.DRAWABLE);
        }
    }

    static class ContentSource extends BitmapImageSource {
        public ContentSource() {
            super(new ContentImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new FileImageFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.CONTENT.length(), fullPath.length());
                }
            })) {
                @Override
                protected void callOnStart(ProgressListener<Bitmap> listener) {
                    // Ignored.
                }
            }, Prefix.CONTENT);
        }
    }

    static class HttpSource extends BitmapImageSource {

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

    static class HttpsSource extends BitmapImageSource {

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
        protected void callOnStart(ProgressListener<Bitmap> listener) {
            // Hooked, won't call.
        }
    }
}
