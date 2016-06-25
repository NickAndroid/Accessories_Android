package dev.nick.imageloader.loader;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.InputStream;

public class AssetsImageFetcher extends BaseImageFetcher {

    AssetManager assets;

    public AssetsImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception {
        String path = splitter.getRealPath(url);

        if (assets == null) assets = context.getAssets();

        InputStream in = assets.open(path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = info.width;
        options.outHeight = info.height;

        return BitmapFactory.decodeStream(in);
    }
}
