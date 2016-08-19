package dev.nick.imageloader.worker.movie;

import android.graphics.Movie;

import dev.nick.imageloader.worker.BaseImageFetcher;
import dev.nick.imageloader.worker.PathSplitter;

public class FileImageFetcher extends BaseImageFetcher<Movie> {

    public FileImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }
}
