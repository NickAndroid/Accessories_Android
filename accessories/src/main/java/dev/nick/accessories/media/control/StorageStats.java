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

package dev.nick.accessories.media.control;

import android.content.Context;

import java.util.concurrent.atomic.AtomicLong;

import dev.nick.accessories.logger.LoggerManager;

public class StorageStats extends UsageStats {

    static final String TAG_INTERNAL_STORAGE = "internal";
    static final String TAG_EXTERNAL_STORAGE = "external";

    static StorageStats sInstance;

    private AtomicLong mExternalUsage, mInternalUsage;

    private StorageStats(Context context) {
        super(context);
        mExternalUsage = new AtomicLong(0);
        mInternalUsage = new AtomicLong(0);
    }

    public synchronized static StorageStats from(Context context) {
        if (sInstance == null) sInstance = new StorageStats(context);
        return sInstance;
    }

    public void onInternalStorageUsage(long size) {
        mInternalUsage.addAndGet(size);
    }

    public void onExternalStorageUsage(long size) {
        mExternalUsage.addAndGet(size);
    }

    public long getInternalStorageUsage() {
        flush(TAG_INTERNAL_STORAGE, mInternalUsage.getAndSet(0));
        return getUsage(TAG_INTERNAL_STORAGE);
    }

    public long getExternalStorageUsage() {
        flush(TAG_EXTERNAL_STORAGE, mExternalUsage.getAndSet(0));
        return getUsage(TAG_EXTERNAL_STORAGE);
    }

    public long getTotalStorageUsage() {
        return getExternalStorageUsage() + getInternalStorageUsage();
    }

    public void flush() {
        LoggerManager.getLogger(getClass()).funcEnter();
        flush(TAG_INTERNAL_STORAGE, mInternalUsage.getAndSet(0));
        flush(TAG_EXTERNAL_STORAGE, mExternalUsage.getAndSet(0));
    }

    public void reset() {
        reset(TAG_EXTERNAL_STORAGE);
        reset(TAG_INTERNAL_STORAGE);
    }
}
