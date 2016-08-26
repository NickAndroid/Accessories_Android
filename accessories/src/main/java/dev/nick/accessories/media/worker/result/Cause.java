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

package dev.nick.accessories.media.worker.result;

import android.support.annotation.Nullable;
import android.util.Log;

public class Cause {

    @Nullable
    public Exception exception;
    @Nullable
    public Error error;

    public Cause(@Nullable Exception e) {
        this.exception = e;
    }

    public Cause(@Nullable Error error) {
        this.error = error;
    }

    public Cause(@Nullable Error error, @Nullable Exception exception) {
        this.error = error;
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "Cause{" +
                "error=" + (error == null ? "NULL" : Log.getStackTraceString(error)) +
                ", exception=" + (exception == null ? "NULL" : Log.getStackTraceString(exception)) +
                '}';
    }
}
