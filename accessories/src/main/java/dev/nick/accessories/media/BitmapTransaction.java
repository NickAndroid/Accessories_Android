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

package dev.nick.accessories.media;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import dev.nick.accessories.media.annotation.AccessoryApi;
import dev.nick.accessories.media.queue.Priority;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.ImageViewDelegate;
import dev.nick.accessories.media.ui.MediaHolder;
import dev.nick.accessories.media.ui.MediaQuality;
import dev.nick.accessories.media.worker.MediaSource;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.bitmap.BitmapMediaSource;
import dev.nick.accessories.media.worker.result.ErrorListener;

public class BitmapTransaction extends Transaction<Bitmap> {

    private static final DisplayOption<Bitmap> sDefDisplayOption = DisplayOption.bitmapBuilder()
            .imageQuality(MediaQuality.OPT)
            .viewMaybeReused()
            .build();

    BitmapTransaction(@NonNull MediaAccessory loader) {
        super(loader);
    }

    @Override
    public BitmapTransaction from(@NonNull String url) {
        super.from(url);
        return this;
    }

    @Override
    MediaSource<Bitmap> onCreateSource(String url) {
        return BitmapMediaSource.from(url);
    }

    @Override
    public BitmapTransaction option(@NonNull DisplayOption<Bitmap> option) {
        super.option(option);
        return this;
    }

    @Override
    public BitmapTransaction progressListener(@NonNull ProgressListener<Bitmap> listener) {
        super.progressListener(listener);
        return this;
    }

    @Override
    public BitmapTransaction errorListener(@NonNull ErrorListener listener) {
        super.errorListener(listener);
        return this;
    }

    @Override
    public BitmapTransaction priority(@NonNull Priority priority) {
        super.priority(priority);
        return this;
    }

    @Override
    public BitmapTransaction into(@NonNull MediaHolder<Bitmap> settable) {
        super.into(settable);
        return this;
    }

    /**
     * @param view The View to display the image.
     * @return Instance of Transaction.
     */
    @AccessoryApi
    public BitmapTransaction into(@NonNull ImageView view) {
        this.settable = new ImageViewDelegate(view);
        return BitmapTransaction.this;
    }


    @Nullable
    @Override
    @AccessoryApi
    public Bitmap startSynchronously() {
        try {
            return accessory.displayBitmap(
                    mediaData,
                    noneNullSettable(),
                    option.or(sDefDisplayOption),
                    progressListener,
                    errorListener,
                    priority)
                    .get();
        } catch (InterruptedException | ExecutionException | CancellationException ignored) {

        }
        return null;
    }

    @Override
    @AccessoryApi
    void startAsync() {
        accessory.displayBitmap(
                mediaData,
                noneNullSettable(),
                option.or(sDefDisplayOption),
                progressListener,
                errorListener,
                priority);
    }


    protected MediaHolder<Bitmap> noneNullSettable() {
        return settable == null ? new FakeBitmapMediaHolder(mediaData.getUrl()) : settable;
    }
}
