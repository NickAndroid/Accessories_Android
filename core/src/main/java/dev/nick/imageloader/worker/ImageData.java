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

public class ImageData<X> {

    private ImageSource<X> source;
    private ImageSink sink;
    private String url;

    public ImageData() {
    }

    public ImageData(ImageSource<X> source, String url) {
        this.source = source;
        this.url = url;
    }

    public ImageSource<X> getSource() {
        return source;
    }

    public void setSink(ImageSink sink) {
        this.sink = sink;
    }

    public ImageSink getSink() {
        return sink;
    }

    public String getUrl() {
        return url;
    }

    public void setSource(ImageSource<X> source) {
        this.source = source;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
