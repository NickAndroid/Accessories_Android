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

package dev.nick.twenty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.ImageQuality;
import dev.nick.imageloader.display.handler.BlurBitmapHandler;

public class DrawableImageTest extends BaseTest {

    final String urlDrawable = "drawable://tree";

    @FindView(id = R.id.image)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawable_list_layout);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageLoader.shared()
                .loadBitmap()
                .from(urlDrawable)
                .option(DisplayOption.builder()
                        .showWithDefault(R.drawable.ic_launcher)
                        .bitmapHandler(new BlurBitmapHandler(24))
                        .imageQuality(ImageQuality.RAW)
                        .build())
                .into(imageView)
                .start();
    }
}
