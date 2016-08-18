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

package dev.nick.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.annotation.CallingOnUIThread;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public interface LoadingListener extends ProgressListener<Bitmap>, ErrorListener {

    @Override
    @CallingOnUIThread
    void onError(@NonNull Cause cause);

    @Override
    @CallingOnUIThread
    void onComplete(@Nullable Bitmap result);

    @Override
    @CallingOnUIThread
    void onProgressUpdate(float progress);

    @Override
    @CallingOnUIThread
    void onStartLoading();

    class Stub implements LoadingListener {

        @Override
        public void onError(@NonNull Cause cause) {
            // Nothing.
        }

        @Override
        public void onComplete(@Nullable Bitmap result) {
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

        }
    }
}
