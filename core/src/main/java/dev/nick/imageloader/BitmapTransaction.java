package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.ui.BitmapImageViewDelegate;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.bitmap.BitmapImageSource;

public class BitmapTransaction extends Transaction<Bitmap> {

    BitmapTransaction(@NonNull ImageLoader loader) {
        super(loader);
    }

    @Override
    public BitmapTransaction from(@NonNull String url) {
        super.from(url);
        return this;
    }

    @Override
    ImageSource<Bitmap> onCreateSource(String url) {
        return BitmapImageSource.from(url);
    }

    @Override
    public BitmapTransaction option(@NonNull DisplayOption<Bitmap> option) {
        super.option(option);
        return this;
    }

    @Override
    public BitmapTransaction listener(@NonNull ProgressListener<Bitmap> listener) {
        super.listener(listener);
        return this;
    }

    @Override
    public BitmapTransaction priority(@NonNull Priority priority) {
        super.priority(priority);
        return this;
    }

    @Override
    public BitmapTransaction into(@NonNull ImageSeat<Bitmap> settable) {
        super.into(settable);
        return this;
    }

    /**
     * @param view The View to display the image.
     * @return Instance of Transaction.
     */
    @LoaderApi
    public BitmapTransaction into(@NonNull ImageView view) {
        this.settable = new BitmapImageViewDelegate(view);
        return BitmapTransaction.this;
    }


    @Nullable
    @Override
    @LoaderApi
    public Bitmap startSynchronously() {
        try {
            return loader.displayBitmap(
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
        loader.displayBitmap(
                imageData,
                noneNullSettable(),
                option,
                progressListener,
                errorListener,
                priority);
    }


    protected ImageSeat<Bitmap> noneNullSettable() {
        return settable == null ? new FakeBitmapImageSeat(imageData.getUrl()) : settable;
    }
}
