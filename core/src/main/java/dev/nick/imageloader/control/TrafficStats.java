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

public class TrafficStats extends UsageStats {

    private static final String TAG_WIFI = "wifi";
    private static final String TAG_MOBILE = "mobile";

    public TrafficStats(Context context) {
        super(context);
    }

    public void onWifiUsage(long size) {
        onUsage(TAG_WIFI, size);
    }

    public void onMobileUsage(long size) {
        onUsage(TAG_MOBILE, size);
    }

    public long getWifiUsage() {
        return getUsage(TAG_WIFI);
    }

    public long getMobileUsage() {
        return getUsage(TAG_MOBILE);
    }

    public long getTotalUsage() {
        return getMobileUsage() + getWifiUsage();
    }
}
