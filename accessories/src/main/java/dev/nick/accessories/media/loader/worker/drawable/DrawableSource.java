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

package dev.nick.accessories.media.loader.worker.drawable;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.media.loader.utils.Preconditions;
import dev.nick.accessories.media.loader.worker.MediaFetcher;
import dev.nick.accessories.media.loader.worker.MediaSource;
import dev.nick.accessories.media.loader.worker.bitmap.BitmapSource;

public class DrawableSource extends MediaSource<Drawable> {

    public static final DrawableSource FILE = new FileSource();
    public static final DrawableSource ASSETS = new AssetsSource();
    public static final DrawableSource DRAWABLE = new DrawableResSource();
    public static final DrawableSource MIPMAP = new MipmapSource();
    public static final DrawableSource CONTENT = new ContentSource();
    public static final DrawableSource HTTP = new HttpSource();
    public static final DrawableSource HTTPS = new HttpsSource();

    private static final List<DrawableSource> DRAWABLE_IMAGE_SOURCES = new ArrayList<>();

    static {
        addDrawableSource(FILE);
        addDrawableSource(ASSETS);
        addDrawableSource(DRAWABLE);
        addDrawableSource(MIPMAP);
        addDrawableSource(CONTENT);
        addDrawableSource(HTTP);
        addDrawableSource(HTTPS);
    }

    public DrawableSource(MediaFetcher<Drawable> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    public static void addDrawableSource(@NonNull DrawableSource source) {
        synchronized (DRAWABLE_IMAGE_SOURCES) {
            String prefix = Preconditions.checkNotNull(source).getPrefix();
            DrawableSource exists = from(prefix);
            DRAWABLE_IMAGE_SOURCES.remove(exists);
            DRAWABLE_IMAGE_SOURCES.add(source);
        }
    }

    public static DrawableSource from(String url) {
        synchronized (DRAWABLE_IMAGE_SOURCES) {
            for (DrawableSource source : DRAWABLE_IMAGE_SOURCES) {
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

    static class FileSource extends DrawableSource {

        public FileSource() {
            super(new OverlayMediaFetcher(BitmapSource.FILE), Prefix.FILE);
        }
    }

    static class AssetsSource extends DrawableSource {

        public AssetsSource() {
            super(new OverlayMediaFetcher(BitmapSource.ASSETS), Prefix.ASSETS);
        }
    }

    static class DrawableResSource extends DrawableSource {

        public DrawableResSource() {
            super(new OverlayMediaFetcher(BitmapSource.DRAWABLE), Prefix.DRAWABLE);
        }
    }

    static class MipmapSource extends DrawableSource {

        public MipmapSource() {
            super(new OverlayMediaFetcher(BitmapSource.MIPMAP), Prefix.MIPMAP);
        }
    }

    static class ContentSource extends DrawableSource {

        public ContentSource() {
            super(new OverlayMediaFetcher(BitmapSource.CONTENT), Prefix.CONTENT);
        }
    }

    static class HttpSource extends DrawableSource {

        public HttpSource() {
            super(new OverlayMediaFetcher(BitmapSource.HTTP), Prefix.HTTP);
        }
    }

    static class HttpsSource extends DrawableSource {

        public HttpsSource() {
            super(new OverlayMediaFetcher(BitmapSource.HTTPS), Prefix.HTTPS);
        }
    }
}
