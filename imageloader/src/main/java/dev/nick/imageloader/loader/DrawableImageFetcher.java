package dev.nick.imageloader.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

public class DrawableImageFetcher extends BaseImageFetcher {

    public DrawableImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception {

        Resources resources = this.context.getResources();

        Log.d("ImageLoader", "real url:" + splitter.getRealPath(url));

        int resId = resources.getIdentifier(splitter.getRealPath(url),
                "drawable",
                this.context.getPackageName());

        Log.d("ImageLoader", "id:" + resId);

        if (resId <= 0) throw new Resources.NotFoundException("Res:" + url);

        return BitmapFactory.decodeResource(resources, resId);
    }
}
