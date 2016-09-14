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

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.os.Build;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import dev.nick.accessories.injection.Injector;
import dev.nick.accessories.logger.LoggerManager;
import dev.nick.accessories.media.loader.LoaderConfig;
import dev.nick.accessories.media.loader.MediaLoader;
import dev.nick.accessories.media.loader.cache.CachePolicy;
import dev.nick.accessories.media.loader.queue.QueuePolicy;
import dev.nick.accessories.media.loader.worker.BaseMediaFetcher;
import dev.nick.accessories.media.loader.worker.DecodeSpec;
import dev.nick.accessories.media.loader.worker.PathSplitter;
import dev.nick.accessories.media.loader.worker.ProgressListener;
import dev.nick.accessories.media.loader.worker.bitmap.BitmapSource;
import dev.nick.accessories.media.loader.worker.movie.MovieSource;
import dev.nick.accessories.media.loader.worker.network.NetworkPolicy;
import dev.nick.accessories.media.loader.worker.result.ErrorListener;

public class MyApp extends Application {
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
                        .enableStorageStats()
                        .cacheDirName("dis.cache.tests")
                        .preferredLocation(CachePolicy.Location.EXTERNAL)
                        .compressFormat(Bitmap.CompressFormat.PNG)
                        .build())
                .networkPolicy(NetworkPolicy.builder()
                        .onlyOnWifi(true)
                        .trafficStatsEnabled(true)
                        .build())
                .debugLevel(Log.VERBOSE)
                .build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        // Add custom sources.
        BitmapSource.addBitmapSource(new BitmapSource(new BaseMediaFetcher<Bitmap>(new PathSplitter<String>() {
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


        MovieSource.addMovieSource(new MovieSource(new BaseMediaFetcher<Movie>(new PathSplitter<String>() {
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

        Injector.createShared(getApplicationContext());
    }
}