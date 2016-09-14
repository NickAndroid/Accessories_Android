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

package dev.nick.accessories.media.loader.worker.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.media.loader.utils.Preconditions;
import dev.nick.accessories.media.loader.worker.MediaFetcher;
import dev.nick.accessories.media.loader.worker.MediaSource;
import dev.nick.accessories.media.loader.worker.PathSplitter;
import dev.nick.accessories.media.loader.worker.ProgressListener;

public class BitmapSource extends MediaSource<Bitmap> {

    public static final BitmapSource FILE = new FileSource();
    public static final BitmapSource ASSETS = new AssetsSource();
    public static final BitmapSource DRAWABLE = new DrawableSource();
    public static final BitmapSource MIPMAP = new MipmapSource();
    public static final BitmapSource CONTENT = new ContentSource();
    public static final BitmapSource HTTP = new HttpSource();
    public static final BitmapSource HTTPS = new HttpsSource();

    private static final List<BitmapSource> BITMAP_IMAGE_SOURCES = new ArrayList<>();

    static {
        addBitmapSource(FILE);
        addBitmapSource(ASSETS);
        addBitmapSource(DRAWABLE);
        addBitmapSource(MIPMAP);
        addBitmapSource(CONTENT);
        addBitmapSource(HTTP);
        addBitmapSource(HTTPS);
    }

    public BitmapSource(MediaFetcher<Bitmap> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    public static void addBitmapSource(@NonNull BitmapSource source) {
        synchronized (BITMAP_IMAGE_SOURCES) {
            String prefix = Preconditions.checkNotNull(source).getPrefix();
            BitmapSource exists = from(prefix);
            BITMAP_IMAGE_SOURCES.remove(exists);
            BITMAP_IMAGE_SOURCES.add(source);
        }
    }

    public static BitmapSource from(String url) {
        synchronized (BITMAP_IMAGE_SOURCES) {
            for (BitmapSource source : BITMAP_IMAGE_SOURCES) {
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

    static class FileSource extends BitmapSource {

        public FileSource() {
            super(new FileMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            }), Prefix.FILE);
        }
    }

    static class AssetsSource extends BitmapSource {

        public AssetsSource() {
            super(new AssetsMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.ASSETS.length(), fullPath.length());
                }
            }), Prefix.ASSETS);
        }
    }

    static class DrawableSource extends BitmapSource {

        public DrawableSource() {
            super(new DrawableMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.DRAWABLE.length(), fullPath.length());
                }
            }), Prefix.DRAWABLE);
        }
    }

    static class MipmapSource extends BitmapSource {

        public MipmapSource() {
            super(new MipmapMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.MIPMAP.length(), fullPath.length());
                }
            }), Prefix.MIPMAP);
        }
    }

    static class ContentSource extends BitmapSource {
        public ContentSource() {
            super(new ContentMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath;
                }
            }, new FileMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            })) {
                @Override
                protected void callOnStart(ProgressListener<Bitmap> listener) {
                    // Ignored.
                }
            }, Prefix.CONTENT);
        }
    }

    static class HttpSource extends BitmapSource {

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

    static class HttpsSource extends BitmapSource {

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
        protected void callOnStart(ProgressListener<Bitmap> listener) {
            // Hooked, won't call.
        }
    }
}
