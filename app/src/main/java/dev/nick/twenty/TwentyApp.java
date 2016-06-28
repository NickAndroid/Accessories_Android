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

package dev.nick.twenty;

import android.content.Context;
import android.os.Environment;

import com.nick.scalpel.ScalpelApplication;

import java.io.File;

import dev.nick.eventbus.EventBus;
import dev.nick.imageloader.ZImageLoader;

public class TwentyApp extends ScalpelApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.create(this);

        ZImageLoader.init(getApplicationContext(), new ZImageLoader.Config()
                .setDebug(true)
                .setPreferExternalStorageCache(true)
                .setCacheThreads(3)
                .setLoadingThreads(1)
                .setEnableFileCache(true)
                .setEnableMemCache(true));
    }

   public static class DataCleanManager {

        public static void cleanInternalCache(Context context) {
            deleteFilesByDirectory(context.getCacheDir());
        }

        public static void cleanDatabases(Context context) {
            deleteFilesByDirectory(new File("/data/data/"
                    + context.getPackageName() + "/databases"));
        }

        public static void cleanSharedPreference(Context context) {
            deleteFilesByDirectory(new File("/data/data/"
                    + context.getPackageName() + "/shared_prefs"));
        }

        public static void cleanDatabaseByName(Context context, String dbName) {
            context.deleteDatabase(dbName);
        }

        public static void cleanFiles(Context context) {
            deleteFilesByDirectory(context.getFilesDir());
        }

        public static void cleanExternalCache(Context context) {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                deleteFilesByDirectory(context.getExternalCacheDir());
            }
        }

        public static void cleanCustomCache(String filePath) {
            deleteFilesByDirectory(new File(filePath));
        }

        public static void cleanApplicationData(Context context, String... filepath) {
            cleanInternalCache(context);
            cleanExternalCache(context);
            cleanDatabases(context);
            cleanSharedPreference(context);
            cleanFiles(context);
            for (String filePath : filepath) {
                cleanCustomCache(filePath);
            }
        }

        private static void deleteFilesByDirectory(File directory) {
            if (directory != null && directory.exists() && directory.isDirectory()) {
                for (File item : directory.listFiles()) {
                    item.delete();
                }
            }
        }
    }
}