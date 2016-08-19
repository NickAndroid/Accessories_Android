package dev.nick.imageloader.worker.movie;

import android.content.res.AssetManager;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;

import dev.nick.imageloader.worker.BaseImageFetcher;
import dev.nick.imageloader.worker.DecodeSpec;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.ErrorListener;

public class FileImageFetcher extends BaseImageFetcher<Movie> {

    public FileImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Movie fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                              @Nullable ProgressListener<Movie> progressListener,
                              @Nullable ErrorListener errorListener) throws Exception {
        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

       return null;
    }
}
