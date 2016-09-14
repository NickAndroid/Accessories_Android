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
package dev.nick.accessories.media.loader.worker.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.accessories.media.loader.worker.BaseMediaFetcher;
import dev.nick.accessories.media.loader.worker.DecodeSpec;
import dev.nick.accessories.media.loader.worker.PathSplitter;
import dev.nick.accessories.media.loader.worker.ProgressListener;
import dev.nick.accessories.media.loader.worker.result.Cause;
import dev.nick.accessories.media.loader.worker.result.ErrorListener;
import dev.nick.accessories.resource.ResourceAccessories;

public abstract class ResourcesMediaFetcher extends BaseMediaFetcher<Bitmap> {

    public ResourcesMediaFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                               @Nullable ProgressListener<Bitmap> progressListener,
                               @Nullable ErrorListener errorListener) throws Exception {
        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        Resources resources = this.mContext.getResources();

        int resId = resources.getIdentifier(mSplitter.getRealPath(url),
                getDefType(),
                this.mContext.getPackageName());

        if (resId <= 0) {
            callOnError(errorListener, new Cause(new Resources.NotFoundException(String.format("Res of id-%s not found for url-%s.", resId, url))));
            return null;
        }

        callOnStart(progressListener);

        Bitmap bitmap;

        try {
            bitmap = ResourceAccessories.getBitmap(mContext, resId);
        } catch (OutOfMemoryError error) {
            callOnError(errorListener, new Cause(error));
            return null;
        }

        callOnComplete(progressListener, bitmap);
        return bitmap;
    }

    abstract
    @NonNull
    String getDefType();
}
