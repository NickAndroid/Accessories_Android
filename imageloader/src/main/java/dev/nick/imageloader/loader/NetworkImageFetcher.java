/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.imageloader.loader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import dev.nick.imageloader.loader.network.NetworkUtils;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class NetworkImageFetcher extends BaseImageFetcher {

    private RequestQueue mRequestQueue;

    public NetworkImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }


    @Override
    public void fetchFromUrl(@NonNull String url,
                             @NonNull DecodeSpec decodeSpec,
                             @Nullable ProgressListener<BitmapResult> progressListener,
                             @Nullable ErrorListener errorListener) throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        boolean wifiOnly = loaderConfig.getNetworkPolicy().isOnlyOnWifi();
        boolean isOnLine = NetworkUtils.isOnline(context, wifiOnly);

        // No connection.
        if (!isOnLine) {
            callOnError(errorListener, new Cause(new IllegalStateException("No network is available.")));
            return;
        }

        callOnStart(progressListener);

        RequestFuture<Bitmap> future = RequestFuture.newFuture();
        ViewSpec viewSpec = decodeSpec.viewSpec;

        ImageRequest imageRequest = new ImageRequest(splitter.getRealPath(url),
                future,
                viewSpec.width, viewSpec.height,
                ImageView.ScaleType.FIT_XY,
                Bitmap.Config.ARGB_8888,
                future);

        if (mRequestQueue == null) mRequestQueue = Volley.newRequestQueue(context);

        mRequestQueue.add(imageRequest);

        Bitmap bitmap = future.get();

        callOnComplete(progressListener, newResult(bitmap));
    }

}
