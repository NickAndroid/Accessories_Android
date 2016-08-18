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

import java.util.concurrent.atomic.AtomicLong;

import dev.nick.imageloader.debug.LoggerManager;

public class TrafficStats extends UsageStats {

    private static final String TAG_WIFI = "wifi";
    private static final String TAG_MOBILE = "mobile";

    static TrafficStats sInstance;

    private AtomicLong mWifiUsage, mMobileUsage;

    private TrafficStats(Context context) {
        super(context);
        mWifiUsage = new AtomicLong(0);
        mMobileUsage = new AtomicLong(0);
    }

    public synchronized static TrafficStats from(Context context) {
        if (sInstance == null) sInstance = new TrafficStats(context);
        return sInstance;
    }

    public void onWifiTrafficUsage(long size) {
        mWifiUsage.addAndGet(size);
    }

    public void onMobileTrafficUsage(long size) {
        mMobileUsage.addAndGet(size);
    }

    public long getWifiTrafficUsage() {
        flush(TAG_WIFI, mWifiUsage.getAndSet(0));
        return getUsage(TAG_WIFI);
    }

    public long getMobileTrafficUsage() {
        flush(TAG_MOBILE, mMobileUsage.getAndSet(0));
        return getUsage(TAG_MOBILE);
    }

    public long getTotalTrafficUsage() {
        return getMobileTrafficUsage() + getWifiTrafficUsage();
    }

    public void flush() {
        LoggerManager.getLogger(getClass()).funcEnter();
        flush(TAG_WIFI, mWifiUsage.getAndSet(0));
        flush(TAG_MOBILE, mMobileUsage.getAndSet(0));
    }

    public void reset() {
        reset(TAG_MOBILE);
        reset(TAG_WIFI);
    }
}
