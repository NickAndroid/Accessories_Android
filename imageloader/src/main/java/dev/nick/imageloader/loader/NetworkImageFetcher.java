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
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import dev.nick.imageloader.display.DisplayOption;

public class NetworkImageFetcher extends BaseImageFetcher {

    private RequestQueue mRequestQueue;

    public NetworkImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, DisplayOption.ImageQuality quality, ImageSpec info) throws Exception {

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
