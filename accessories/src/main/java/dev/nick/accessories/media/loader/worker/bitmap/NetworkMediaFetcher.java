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

package dev.nick.accessories.media.loader.worker.bitmap;

import android.graphics.Bitmap;

import dev.nick.accessories.media.loader.worker.BaseNetworkMediaFetcher;
import dev.nick.accessories.media.loader.worker.MediaFetcher;
import dev.nick.accessories.media.loader.worker.PathSplitter;

public class NetworkMediaFetcher extends BaseNetworkMediaFetcher<Bitmap> {

    public NetworkMediaFetcher(PathSplitter<String> splitter, MediaFetcher<Bitmap> fileMediaFetcher) {
        super(splitter, fileMediaFetcher);
    }
}