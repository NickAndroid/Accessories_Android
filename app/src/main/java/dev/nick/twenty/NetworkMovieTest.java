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

import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Animation;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;

import dev.nick.accessories.media.MediaAccessory;
import dev.nick.accessories.media.queue.Priority;
import dev.nick.accessories.media.ui.MediaHolder;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.result.Cause;
import dev.nick.accessories.media.worker.result.ErrorListener;
import dev.nick.accessories.logger.LoggerManager;

public class NetworkMovieTest extends BaseTest {

    final String urlHttps = "https://camo.githubusercontent.com/0be34709e630f7cbb96012fb1a48139bc5d45f07/68747470733a2f2f7777772e676f6f676c652e636f6d2f6c6f676f732f646f6f646c65732f323031362f74656163686572732d6461792d323031362d75732d363239363632363234343039313930342e322d687032782e676966";

    @FindView(id = R.id.movie)
    SimpleGifView simpleGifView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_movie_layout);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaAccessory.shared()
                .loadMovie()
                .from(urlHttps)
                .errorListener(new ErrorListener() {
                    @Override
                    public void onError(@NonNull Cause cause) {
                        LoggerManager.getLogger(getClass()).error(cause);
                    }
                })
                .progressListener(new ProgressListener<Movie>() {
                    @Override
                    public void onStartLoading() {
                        LoggerManager.getLogger(getClass()).debug("onStartLoading");
                    }

                    @Override
                    public void onProgressUpdate(float progress) {
                        LoggerManager.getLogger(getClass()).debug("onProgressUpdate:" + progress);
                        setTitle(String.valueOf(progress * 100));
                    }

                    @Override
                    public void onCancel() {
                        LoggerManager.getLogger(getClass()).debug("onCancel");
                    }

                    @Override
                    public void onComplete(Movie result) {
                        LoggerManager.getLogger(getClass()).debug("onComplete:" + result);
                    }
                })
                .into(new MediaHolder<Movie>() {
                    @Override
                    public void seat(@NonNull Movie image) {
                        LoggerManager.getLogger(getClass()).debug("seat");
                        simpleGifView.setMovie(image);
                    }

                    @Override
                    public int getWidth() {
                        return 1000;
                    }

                    @Override
                    public int getHeight() {
                        return 500;
                    }

                    @Override
                    public void startAnimation(Animation animation) {
                        LoggerManager.getLogger(getClass()).debug("startAnimation");
                    }
                })
                .priority(Priority.HIGH)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaAccessory.shared().cancel(urlHttps);
    }
}
