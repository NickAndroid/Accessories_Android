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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import dev.nick.imageloader.MediaLoader;
import dev.nick.imageloader.ProgressListenerStub;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;
import dev.nick.logger.LoggerManager;

public class DownloadMachine extends BaseTest {

    final String urlHost = "http://i.imgur.com/";

    @FindView(id = R.id.image)
    ImageView imageView;

    CountDownLatch latch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_layout);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
               while (true) {
                   startMachine(urlHost + randomString(7) + ".png");
                   try {
                       latch.await();
                   } catch (InterruptedException e) {

                   }
               }
            }
        }).start();
    }

    void startMachine(String url) {
        latch = new CountDownLatch(1);
        MediaLoader.shared()
                .loadBitmap()
                .from(url)
                .progressListener(new ProgressListenerStub<Bitmap>() {
                    @Override
                    public void onComplete(@Nullable Bitmap result) {
                        super.onComplete(result);
                        latch.countDown();
                        if (result != null)
                            LoggerManager.getLogger(DownloadMachine.class).debug("New img:" + result);
                    }

                    @Override
                    public void onCancel() {
                        super.onCancel();
                        latch.countDown();
                    }
                })
                .errorListener(new ErrorListener() {
                    @Override
                    public void onError(@NonNull Cause cause) {
                        latch.countDown();
                    }
                })
                .priority(Priority.HIGH)
                .into(imageView)
                .start();
    }

    public String randomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int num = random.nextInt(62);
            buf.append(str.charAt(num));
        }

        return buf.toString();
    }
}
