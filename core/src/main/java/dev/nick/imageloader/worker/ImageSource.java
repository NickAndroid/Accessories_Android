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

package dev.nick.imageloader.worker;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.imageloader.LoaderConfig;

public abstract class ImageSource<X> {

    private ImageFetcher<X> fetcher;
    private String prefix;

    public ImageSource(ImageFetcher<X> fetcher, String prefix) {
        this.fetcher = fetcher;
        this.prefix = prefix;
    }

    @NonNull
    public ImageFetcher<X> getFetcher(Context context, LoaderConfig config) {
        return fetcher.prepare(context, config);
    }

    public boolean isOneOf(@NonNull ImageSource... sources) {
        for (ImageSource source : sources) {
            if (source.equals(this)) return true;
        }
        return false;
    }

    public String getPrefix() {
        return prefix;
    }

    public abstract boolean maybeSlow();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageSource<?> that = (ImageSource<?>) o;

        return prefix != null ? prefix.equals(that.prefix) : that.prefix == null;
    }

    @Override
    public int hashCode() {
        return prefix != null ? prefix.hashCode() : 0;
    }

    public interface Prefix {
        String FILE = "file://";
        String ASSETS = "assets://";
        String DRAWABLE = "drawable://";
        String CONTENT = "content://";
        String HTTP = "http://";
        String HTTPS = "https://";
    }
}
