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

package dev.nick.accessories.media.loader;

import android.support.annotation.Nullable;

import dev.nick.accessories.media.loader.worker.ProgressListener;

public class ProgressListenerStub<T> implements ProgressListener<T> {

    @Override
    public void onComplete(@Nullable T result) {
        // Nothing.
    }

    @Override
    public void onProgressUpdate(float progress) {
        // Nothing.
    }

    @Override
    public void onCancel() {
        // Nothing.
    }

    @Override
    public void onStartLoading() {
        // Nothing.
    }
}
