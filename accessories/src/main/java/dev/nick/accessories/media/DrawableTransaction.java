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

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import dev.nick.accessories.common.annotation.AccessoryApi;
import dev.nick.accessories.media.queue.Priority;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.DrawableImageViewDelegate;
import dev.nick.accessories.media.ui.MediaHolder;
import dev.nick.accessories.media.worker.MediaSource;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.drawable.DrawableSource;
import dev.nick.accessories.media.worker.result.ErrorListener;

@Deprecated
public class DrawableTransaction extends Transaction<Drawable> {

    DrawableTransaction(@NonNull MediaLoader loader) {
        super(loader);
    }

    @Override
    public DrawableTransaction from(@NonNull String url) {
        super.from(url);
        return this;
    }

    @Override
    MediaSource<Drawable> onCreateSource(String url) {
        return DrawableSource.from(url);
    }

    @Override
    public DrawableTransaction option(@NonNull DisplayOption<Drawable> option) {
        super.option(option);
        return this;
    }

    @Override
    public DrawableTransaction progressListener(@NonNull ProgressListener<Drawable> listener) {
        super.progressListener(listener);
        return this;
    }

    @Override
    public DrawableTransaction errorListener(@NonNull ErrorListener listener) {
        super.errorListener(listener);
        return this;
    }

    @Override
    public DrawableTransaction priority(@NonNull Priority priority) {
        super.priority(priority);
        return this;
    }

    @Override
    public DrawableTransaction into(@NonNull MediaHolder<Drawable> settable) {
        super.into(settable);
        return this;
    }

    /**
     * @param view The View to display the image.
     * @return Instance of Transaction.
     */
    @AccessoryApi
    public DrawableTransaction into(@NonNull ImageView view) {
        this.settable = new DrawableImageViewDelegate(view);
        return DrawableTransaction.this;
    }


    @Nullable
    @Override
    @AccessoryApi
    public Drawable startSynchronously() {
        throw new UnsupportedOperationException();
    }

    @Override
    @AccessoryApi
    void startAsync() {
        throw new UnsupportedOperationException();
    }

}
