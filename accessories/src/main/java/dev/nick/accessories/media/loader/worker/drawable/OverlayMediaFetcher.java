package dev.nick.accessories.media.loader.worker.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.accessories.media.loader.worker.BaseMediaFetcher;
import dev.nick.accessories.media.loader.worker.DecodeSpec;
import dev.nick.accessories.media.loader.worker.ProgressListener;
import dev.nick.accessories.media.loader.worker.bitmap.BitmapSource;
import dev.nick.accessories.media.loader.worker.result.ErrorListener;
import dev.nick.accessories.resource.ResourceAccessories;

public class OverlayMediaFetcher extends BaseMediaFetcher<Drawable> {

    private BitmapSource bitmapSource;

    public OverlayMediaFetcher(@NonNull BitmapSource bitmapSource) {
        super(null);
        this.bitmapSource = bitmapSource;
    }

    @Override
    public Drawable fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                                 @Nullable ProgressListener<Drawable> progressListener,
                                 @Nullable ErrorListener errorListener) throws Exception {

        Bitmap out = bitmapSource.getFetcher(mContext, mLoaderConfig).fetchFromUrl(url, decodeSpec,
                new OverlayProgressListener(progressListener), errorListener);
        if (out == null) return null;
        return ResourceAccessories.bitmapToDrawable(out, mContext.getResources());
    }
}
