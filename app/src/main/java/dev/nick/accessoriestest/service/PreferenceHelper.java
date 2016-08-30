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

package dev.nick.accessoriestest.service;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nick on 16-2-8.
 * Email: nick.guo.dev@icloud.com
 * Github: https://github.com/NickAndroid
 */
public class PreferenceHelper {

    private static final String PERF_NAME = "com.nick.app.heng.prefs";
    private static final String PREF_FIRST_RUN = PERF_NAME + ".first.run";
    private static final String PREF_PLAY_MODE = PERF_NAME + ".play.mode";

    private static PreferenceHelper sHelper;

    private SharedPreferences mPrefs;

    private PreferenceHelper(Context context) {
        mPrefs = context.getSharedPreferences(PERF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferenceHelper from(Context context) {
        if (sHelper == null) sHelper = new PreferenceHelper(context);
        return sHelper;
    }

    public boolean isFirstRun() {
        return mPrefs.getBoolean(PREF_FIRST_RUN, true);
    }

    public void setFirstRun(boolean first) {
        mPrefs.edit().putBoolean(PREF_FIRST_RUN, first).apply();
    }

    public int getPlayMode() {
        return mPrefs.getInt(PREF_PLAY_MODE, MediaPlayerService.PlayMode.MODE_LIST);
    }

    public void setPlayMode(int mode) {
        mPrefs.edit().putInt(PREF_PLAY_MODE, mode).apply();
    }
}
