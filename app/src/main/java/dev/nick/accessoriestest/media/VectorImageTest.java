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

package dev.nick.accessoriestest.media;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import dev.nick.accessories.injection.Injector;
import dev.nick.accessories.injection.annotation.binding.BindView;
import dev.nick.accessories.logger.LoggerManager;
import dev.nick.accessories.media.loader.MediaLoader;
import dev.nick.accessories.media.loader.ui.DisplayOption;
import dev.nick.accessories.media.loader.ui.MediaQuality;
import dev.nick.accessories.media.loader.worker.result.Cause;
import dev.nick.accessories.media.loader.worker.result.ErrorListener;
import dev.nick.accessoriestest.R;

public class VectorImageTest extends BaseTest {

    final String urlDrawable = "drawable://ic_help_black_24dp";

    @BindView(R.id.image)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_layout);
        setTitle(getClass().getSimpleName());
        Injector.shared().inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaLoader.shared()
                .loadBitmap()
                .from(urlDrawable)
                .option(DisplayOption.bitmapBuilder()
                        .showOnFailure(BitmapFactory.decodeResource(getResources(), R.drawable.aio_image_fail))
                        .imageQuality(MediaQuality.OPT)
                        .build())
                .errorListener(new ErrorListener() {
                    @Override
                    public void onError(@NonNull Cause cause) {
                        LoggerManager.getLogger(VectorImageTest.class).warn(cause);
                    }
                })
                .into(imageView)
                .start();
    }
}
