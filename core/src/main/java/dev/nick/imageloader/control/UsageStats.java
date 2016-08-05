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

import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class UsageStats {

    protected Context mContext;
    protected Logger mLogger;

    public UsageStats(Context context) {
        this.mContext = context;
        this.mLogger = LoggerManager.getLogger(getClass());
    }

    protected void writeString(String tag, String value) {
        mContext.getSharedPreferences(buildTag(), Context.MODE_PRIVATE).edit().putString(tag, value).apply();
    }

    protected String readString(String tag, String defValue) {
        return mContext.getSharedPreferences(buildTag(), Context.MODE_PRIVATE).getString(tag, defValue);
    }

    protected String buildTag() {
        return mContext.getPackageName() + "." + getClass().getSimpleName().toLowerCase();
    }

    protected long getUsage(String tag) {
        String last = readString(tag, String.valueOf(0));
        return Long.parseLong(last);
    }

    protected void onUsage(String tag, long size) {
        mLogger.verbose(tag + "-" + size);
        String last = readString(tag, String.valueOf(0));
        long lastLong = Long.parseLong(last);
        writeString(tag, String.valueOf((lastLong + size)));
    }
}
