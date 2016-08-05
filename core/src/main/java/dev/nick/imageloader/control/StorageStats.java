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

package dev.nick.imageloader.control;

import android.content.Context;

public class StorageStats extends UsageStats {

    static final String TAG_INTERNAL_STORAGE = "internal";
    static final String TAG_EXTERNAL_STORAGE = "external";

    public StorageStats(Context context) {
        super(context);
    }

    public void onInternalStorageUsage(long size) {
        onUsage(TAG_INTERNAL_STORAGE, size);
    }

    public void onExternalStorageUsage(long size) {
        onUsage(TAG_EXTERNAL_STORAGE, size);
    }

    public long getInternalStorageUsage() {
        return getUsage(TAG_INTERNAL_STORAGE);
    }

    public long getExternalStorageUsage() {
        return getUsage(TAG_EXTERNAL_STORAGE);
    }

    public long getTotalStorageUsage() {
        return getExternalStorageUsage() + getInternalStorageUsage();
    }

    public void reset() {
        onReset(TAG_EXTERNAL_STORAGE);
        onReset(TAG_INTERNAL_STORAGE);
    }
}
