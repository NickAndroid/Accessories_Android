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

package dev.nick.accessoriestest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;

import dev.nick.accessories.media.MediaAccessory;
import dev.nick.accessories.media.ProgressListenerStub;
import dev.nick.accessories.media.queue.Priority;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.MediaQuality;
import dev.nick.accessories.media.ui.animator.FadeInViewAnimator;
import dev.nick.accessories.media.ui.art.BlackWhiteMediaArt;

public class AssetsImageTest extends BaseTest {

    final String urlAssets = "assets://tree.jpg";

    @FindView(id = R.id.image)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_layout);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaAccessory.shared()
                .loadBitmap()
                .from(urlAssets)
                .progressListener(new ProgressListenerStub<Bitmap>() {
                    @Override
                    public void onComplete(@Nullable Bitmap result) {
                        super.onComplete(result);
                    }
                })
                .option(DisplayOption.bitmapBuilder()
                        .showOnFailure(BitmapFactory.decodeResource(getResources(), R.drawable.aio_image_fail))
                        .imageArt(new BlackWhiteMediaArt())
                        .imageQuality(MediaQuality.RAW)
                        .imageAnimator(new FadeInViewAnimator())
                        .build())
                .priority(Priority.HIGH)
                .into(imageView)
                .start();
    }
}
