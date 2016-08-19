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
