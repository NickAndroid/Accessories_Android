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

import com.nick.scalpel.ScalpelApplication;

import dev.nick.eventbus.EventBus;
import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.CachePolicy;

public class TwentyApp extends ScalpelApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.create(this);
        ImageLoader.init(getApplicationContext(), new LoaderConfig.Builder()
                .cachePolicy(new CachePolicy.Builder().preferredLocation(CachePolicy.Location.EXTERNAL)
                        .build())
                .cachingThreads(Runtime.getRuntime().availableProcessors())
                .loadingThreads(Runtime.getRuntime().availableProcessors() * 2)
                .diskCacheEnabled(true)
                .memCacheEnabled(true)
                .debug(true)
                .build());
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ImageLoader.getInstance().clearMemCache();
    }
}