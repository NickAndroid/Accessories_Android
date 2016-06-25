package dev.nick.imageloader.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

class BaseImageFetcher implements ImageFetcher {

    protected PathSplitter<String> splitter;

    protected Context context;

    public BaseImageFetcher(PathSplitter<String> splitter) {
        this.splitter = splitter;
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception {
        return null;
    }

    @Override
    public void attachContext(Context context) {
        this.context = context;
    }

    protected void logW(Object msg) {
        Log.d("Nick", String.valueOf(msg));
    }
}
