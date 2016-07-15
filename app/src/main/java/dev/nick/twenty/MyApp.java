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
import android.util.Log;

import com.nick.scalpel.ScalpelApplication;

import dev.nick.eventbus.EventBus;
import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.CachePolicy;

public class MyApp extends ScalpelApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.create(this);
        ImageLoader.init(getApplicationContext(), new LoaderConfig.Builder()
                .cachePolicy(new CachePolicy.Builder()
                        .enableMemCache()
                        .enableDiskCache()
                        .cachingThreads(Runtime.getRuntime().availableProcessors())
                        .cacheDirName("dis.cache.tests")
                        .preferredLocation(CachePolicy.Location.EXTERNAL)
                        .compressFormat(Bitmap.CompressFormat.PNG)
                        .build())
                .debugLevel(Log.VERBOSE)
                .loadingThreads(Runtime.getRuntime().availableProcessors() * 2)
                .build());
    }
}