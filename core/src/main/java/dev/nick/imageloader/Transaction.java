package dev.nick.imageloader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ImageSourceType;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.ui.BitmapImageSeat;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.utils.Preconditions;

public abstract class Transaction<T> {

    protected ImageSource<T> imageSource;
    protected DisplayOption<T> option;
    protected ProgressListener<T> progressListener;
    protected ErrorListener errorListener;
    protected Priority priority;
    protected ImageSeat<T> settable;

    protected ImageLoader loader;

    Transaction(@NonNull ImageLoader loader) {
        this.loader = loader;
    }

    /**
     * @param url Image source from, one of {@link ImageSourceType}
     * @return Instance of Transaction.
     */
    @LoaderApi
    public Transaction<T> from(@NonNull String url) {
        ImageSourceType<T> type = ImageSourceType.of(Preconditions.checkNotNull(url));
        this.imageSource = new ImageSource<>(type, url);
        return Transaction.this;
    }

    /**
     * @param option {@link DisplayOption} is options using when display the image.
     *               * @param settable Target {@link BitmapImageSeat} to display the image.
     * @return Instance of Transaction.
     */
    @LoaderApi
    public Transaction<T> option(@NonNull DisplayOption<T> option) {
        this.option = Preconditions.checkNotNull(option);
        return Transaction.this;
    }

    /**
     * @param listener The progress progressListener using to watch the progress of the loading.
     * @return Instance of Transaction.
     */
    @LoaderApi
    public Transaction<T> listener(@NonNull ProgressListener<T> listener) {
        this.progressListener = Preconditions.checkNotNull(listener);
        return Transaction.this;
    }

    /**
     * @param priority Priority for this task.
     * @return Instance of Transaction.
     */
    @LoaderApi
    public Transaction<T> priority(@NonNull Priority priority) {
        this.priority = Preconditions.checkNotNull(priority);
        return Transaction.this;
    }

    /**
     * @param settable The View to display the image.
     * @return Instance of Transaction.
     */
    @LoaderApi
    public Transaction<T> into(@NonNull ImageSeat<T> settable) {
        this.settable = Preconditions.checkNotNull(settable);
        return Transaction.this;
    }

    /**
     * Call this to start this transaction.
     */
    @LoaderApi
    public void start() {
        this.loader.getTransactionService().push(this);
    }

    /**
     * Call this to start this transaction synchronously.
     *
     * @return Result for this transaction.
     */
    public
    @Nullable
    @LoaderApi
    abstract T startSynchronously();

    abstract void startAsync();
}
