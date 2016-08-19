package dev.nick.imageloader.worker.movie;

import android.content.res.AssetManager;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import dev.nick.imageloader.worker.BaseImageFetcher;
import dev.nick.imageloader.worker.DecodeSpec;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class AssetsImageFetcher extends BaseImageFetcher<Movie> {

    AssetManager mAssets;

    public AssetsImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Movie fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec, @Nullable ProgressListener<Movie> progressListener, @Nullable ErrorListener errorListener) throws Exception {
        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        String path = mSplitter.getRealPath(url);

        if (mAssets == null) mAssets = mContext.getAssets();

        InputStream in = null;

        try {
            in = mAssets.open(path);
            callOnStart(progressListener);
            Movie result = Movie.decodeStream(in);
            callOnComplete(progressListener, result);
            return result;
        } catch (IOException e) {
            callOnError(errorListener, new Cause(e));
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
