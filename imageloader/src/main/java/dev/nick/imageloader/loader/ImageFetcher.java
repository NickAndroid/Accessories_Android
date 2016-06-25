package dev.nick.imageloader.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public interface ImageFetcher {
    Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception;

    void attachContext(Context context);
}
