package dev.nick.imageloader;

import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.movie.MovieImageSource;
import dev.nick.imageloader.worker.result.ErrorListener;

public class MovieTransaction extends Transaction<Movie> {

    MovieTransaction(@NonNull ImageLoader loader) {
        super(loader);
    }

    @Override
    public MovieTransaction from(@NonNull String url) {
        super.from(url);
        return this;
    }

    @Override
    ImageSource<Movie> onCreateSource(String url) {
        return MovieImageSource.from(url);
    }

    @Override
    public MovieTransaction option(@NonNull DisplayOption<Movie> option) {
        super.option(option);
        return this;
    }

    @Override
    public MovieTransaction progressListener(@NonNull ProgressListener<Movie> listener) {
        super.progressListener(listener);
        return this;
    }

    @Override
    public MovieTransaction errorListener(@NonNull ErrorListener listener) {
        super.errorListener(listener);
        return this;
    }

    @Override
    public MovieTransaction priority(@NonNull Priority priority) {
        super.priority(priority);
        return this;
    }

    @Override
    public MovieTransaction into(@NonNull ImageSeat<Movie> settable) {
        super.into(settable);
        return this;
    }

    @Nullable
    @Override
    @LoaderApi
    public Movie startSynchronously() {
        try {
            return loader.displayMovie(
                    imageData,
                    noneNullSettable(),
                    option,
                    progressListener,
                    errorListener,
                    priority)
                    .get();
        } catch (InterruptedException | ExecutionException | CancellationException ignored) {

        }
        return null;
    }

    @Override
    @LoaderApi
    void startAsync() {
        loader.displayMovie(
                imageData,
                noneNullSettable(),
                option,
                progressListener,
                errorListener,
                priority);
    }


    protected ImageSeat<Movie> noneNullSettable() {
        return settable == null ? new FakeMovieImageSeat(imageData.getUrl()) : settable;
    }
}
