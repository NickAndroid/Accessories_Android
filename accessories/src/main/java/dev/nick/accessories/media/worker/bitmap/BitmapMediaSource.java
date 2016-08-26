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

package dev.nick.accessories.media.worker.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.media.utils.Preconditions;
import dev.nick.accessories.media.worker.MediaFetcher;
import dev.nick.accessories.media.worker.MediaSource;
import dev.nick.accessories.media.worker.PathSplitter;
import dev.nick.accessories.media.worker.ProgressListener;

public class BitmapMediaSource extends MediaSource<Bitmap> {

    public static final BitmapMediaSource FILE = new FileSource();
    public static final BitmapMediaSource ASSETS = new AssetsSource();
    public static final BitmapMediaSource DRAWABLE = new DrawableSource();
    public static final BitmapMediaSource MIPMAP = new MipmapSource();
    public static final BitmapMediaSource CONTENT = new ContentSource();
    public static final BitmapMediaSource HTTP = new HttpSource();
    public static final BitmapMediaSource HTTPS = new HttpsSource();

    private static final List<BitmapMediaSource> BITMAP_IMAGE_SOURCES = new ArrayList<>();

    static {
        addBitmapSource(FILE);
        addBitmapSource(ASSETS);
        addBitmapSource(DRAWABLE);
        addBitmapSource(MIPMAP);
        addBitmapSource(CONTENT);
        addBitmapSource(HTTP);
        addBitmapSource(HTTPS);
    }

    public BitmapMediaSource(MediaFetcher<Bitmap> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    public static void addBitmapSource(@NonNull BitmapMediaSource source) {
        synchronized (BITMAP_IMAGE_SOURCES) {
            String prefix = Preconditions.checkNotNull(source).getPrefix();
            BitmapMediaSource exists = from(prefix);
            BITMAP_IMAGE_SOURCES.remove(exists);
            BITMAP_IMAGE_SOURCES.add(source);
        }
    }

    public static BitmapMediaSource from(String url) {
        synchronized (BITMAP_IMAGE_SOURCES) {
            for (BitmapMediaSource source : BITMAP_IMAGE_SOURCES) {
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

    static class FileSource extends BitmapMediaSource {

        public FileSource() {
            super(new FileMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.FILE.length(), fullPath.length());
                }
            }), Prefix.FILE);
        }
    }

    static class AssetsSource extends BitmapMediaSource {

        public AssetsSource() {
            super(new AssetsMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.ASSETS.length(), fullPath.length());
                }
            }), Prefix.ASSETS);
        }
    }

    static class DrawableSource extends BitmapMediaSource {

        public DrawableSource() {
            super(new DrawableMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.DRAWABLE.length(), fullPath.length());
                }
            }), Prefix.DRAWABLE);
        }
    }

    static class MipmapSource extends BitmapMediaSource {

        public MipmapSource() {
            super(new MipmapMediaFetcher(new PathSplitter<String>() {
                @Override
                public String getRealPath(@NonNull String fullPath) {
                    return fullPath.substring(Prefix.MIPMAP.length(), fullPath.length());
                }
            }), Prefix.MIPMAP);
        }
    }

    static class ContentSource extends BitmapMediaSource {
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

    static class HttpSource extends BitmapMediaSource {

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

    static class HttpsSource extends BitmapMediaSource {

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
