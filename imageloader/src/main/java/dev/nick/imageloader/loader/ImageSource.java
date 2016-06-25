package dev.nick.imageloader.loader;

import android.content.Context;
import android.support.annotation.NonNull;

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
            return fullPath.substring(CONTENT.prefix.length(), fullPath.length());
        }
    }, new FileImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath.substring(FILE.prefix.length(), fullPath.length());
        }
    })), "content://"),

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

    NETWORK(new NetworkImageFetcher(new PathSplitter<String>() {
        @Override
        public String getRealPath(@NonNull String fullPath) {
            return fullPath;
        }
    }), "http://"),

    UNKNOWN(null, null);

    ImageFetcher fetcher;
    String prefix;

    ImageSource(ImageFetcher fetcher, String prefix) {
        this.fetcher = fetcher;
        this.prefix = prefix;
    }

    public ImageFetcher getFetcher(Context context) {
        fetcher.attachContext(context);
        return fetcher;
    }

    public static ImageSource of(@NonNull String url) {
        for (ImageSource source : ImageSource.values()) {
            if (url.startsWith(source.prefix)) return source;
        }
        return ImageSource.UNKNOWN;
    }
}
