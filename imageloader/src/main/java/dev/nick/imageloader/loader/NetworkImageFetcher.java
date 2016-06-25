package dev.nick.imageloader.loader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

public class NetworkImageFetcher extends BaseImageFetcher {

   private RequestQueue mRequestQueue;

    public NetworkImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception {

        RequestFuture<Bitmap> future = RequestFuture.newFuture();

        ImageRequest imageRequest = new ImageRequest(splitter.getRealPath(url),
                future,
                info.width, info.height,
                ImageView.ScaleType.FIT_XY,
                Bitmap.Config.ARGB_8888,
                future);


        if (mRequestQueue == null) mRequestQueue = Volley.newRequestQueue(context);

        mRequestQueue.add(imageRequest);

        return future.get();
    }
}
