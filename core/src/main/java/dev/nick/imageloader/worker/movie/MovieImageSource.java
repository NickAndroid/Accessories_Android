package dev.nick.imageloader.worker.movie;

import android.graphics.Movie;
import android.support.annotation.NonNull;

import dev.nick.imageloader.worker.ImageFetcher;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.PathSplitter;

public class MovieImageSource extends ImageSource<Movie> {

    public static final MovieImageSource FILE = new FileSource();

    public MovieImageSource(ImageFetcher<Movie> fetcher, String prefix) {
        super(fetcher, prefix);
    }

    @Override
    public boolean maybeSlow() {
        return false;
    }

    public static MovieImageSource from(String url) {
        return FILE;
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
}
