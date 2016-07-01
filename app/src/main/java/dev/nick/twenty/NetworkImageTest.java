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
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.animator.FadeInImageAnimator;
import dev.nick.imageloader.display.processor.BlurBitmapProcessor;

public class NetworkImageTest extends BaseTest {

    final String urlHttp = "http://tse2.mm.bing.net/th?id=OIP.M960c6796f4870a8764558c39e9148afao2&pid=15.1";

    @FindView(id = R.id.image)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawable_list);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageLoader.getInstance().displayImage(urlHttp, imageView, new DisplayOption.Builder()
                .defaultImgRes(R.drawable.ic_launcher)
                .bitmapProcessor(new BlurBitmapProcessor())
                .imageQuality(DisplayOption.ImageQuality.RAW)
                .imageAnimator(new FadeInImageAnimator())
                .build());
    }
}
