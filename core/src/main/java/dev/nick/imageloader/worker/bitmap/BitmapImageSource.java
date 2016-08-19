package dev.nick.imageloader.worker.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

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

    public BitmapImageSource(ImageFetcher<Bitmap> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    @Override
    public boolean maybeSlow() {
        return false;
    }

    public static BitmapImageSource from(String url) {
        return FILE;
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
