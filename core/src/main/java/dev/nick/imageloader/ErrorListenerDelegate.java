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

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.InterruptedIOException;

import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.ui.ImageChair;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

abstract class ErrorListenerDelegate<T> implements ErrorListener {

    ErrorListener listener;

    T failureImg;

    ImageChair<T> imageChair;

    public ErrorListenerDelegate(ErrorListener listener, T failureImg, ImageChair<T> seat) {
        this.listener = listener;
        this.failureImg = failureImg;
        this.imageChair = seat;
    }

    @Override
    public void onError(@NonNull Cause cause) {
        if (LoggerManager.getDebugLevel() <= Log.ASSERT) {
            LoggerManager.getLogger(getClass()).warn(cause);
        }
        if (cause.exception instanceof InterruptedIOException) {
            // It's ok, We canceled this task.
        } else {
            if (failureImg != null) {
                onApplyFailureImage(failureImg);
            }
            UIThreadRouter.getSharedRouter().callOnFailure(listener, cause);
        }
    }

    abstract void onApplyFailureImage(T image);
}
