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
import android.graphics.Movie;
import android.os.Build;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nick.scalpel.ScalpelApplication;

import dev.nick.imageloader.MediaLoader;
import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.queue.QueuePolicy;
import dev.nick.imageloader.worker.BaseImageFetcher;
import dev.nick.imageloader.worker.DecodeSpec;
import dev.nick.imageloader.worker.PathSplitter;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.bitmap.BitmapImageSource;
import dev.nick.imageloader.worker.movie.MovieImageSource;
import dev.nick.imageloader.worker.network.NetworkPolicy;
import dev.nick.imageloader.worker.result.ErrorListener;
import dev.nick.logger.LoggerManager;

public class MyApp extends ScalpelApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        LoggerManager.setTagPrefix(getClass().getSimpleName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.beginSection("ImageLoader_init");
        }
        MediaLoader.createShared(getApplicationContext(), LoaderConfig.builder()
                .queuePolicy(QueuePolicy.LIFO)
                .cachePolicy(CachePolicy.builder()
                        .enableMemCache()
                        .enableDiskCache()
                        .enableStorgeStats()
                        .cacheDirName("dis.cache.tests")
                        .preferredLocation(CachePolicy.Location.EXTERNAL)
                        .compressFormat(Bitmap.CompressFormat.PNG)
                        .build())
                .networkPolicy(NetworkPolicy.builder()
                        .onlyOnWifi()
                        .enableTrafficStats().build())
                .debugLevel(Log.VERBOSE)
                .build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        // Add custom sources.
        BitmapImageSource.addBitmapSource(new BitmapImageSource(new BaseImageFetcher<Bitmap>(new PathSplitter<String>() {
            @Override
            public String getRealPath(@NonNull String fullPath) {
                return null;
            }
        }) {
            @Override
            public Bitmap fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                                       @Nullable ProgressListener<Bitmap> progressListener,
                                       @Nullable ErrorListener errorListener) throws Exception {
                LoggerManager.getLogger(getClass()).funcEnter();
                return super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);
            }
        }, "test_bitmap://"));


        MovieImageSource.addMovieSource(new MovieImageSource(new BaseImageFetcher<Movie>(new PathSplitter<String>() {
            @Override
            public String getRealPath(@NonNull String fullPath) {
                return null;
            }
        }) {
            @Override
            public Movie fetchFromUrl(@NonNull String url, @NonNull DecodeSpec decodeSpec,
                                      @Nullable ProgressListener<Movie> progressListener,
                                      @Nullable ErrorListener errorListener) throws Exception {
                LoggerManager.getLogger(getClass()).funcEnter();
                return super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);
            }
        }, "test_movie://"));
    }
}