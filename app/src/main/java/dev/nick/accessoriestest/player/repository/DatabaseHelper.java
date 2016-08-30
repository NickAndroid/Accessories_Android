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

package dev.nick.accessoriestest.player.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nick on 16-2-10.
 * Email: nick.guo.dev@icloud.com
 * Github: https://github.com/NickAndroid
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "app_heng";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String liked = "CREATE TABLE "
                + LikedColumns.TABLE_NAME
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + BaseColumns.COLUMN_TITLE +
                " TEXT," +
                BaseColumns.COLUMN_ARTIST +
                " TEXT," +
                BaseColumns.COLUMN_ALBUM +
                " TEXT," +
                BaseColumns.COLUMN_DURATION +
                " TEXT," +
                BaseColumns.COLUMN_URL +
                " TEXT," +
                BaseColumns.COLUMN_SONG_ID +
                " INTEGER," +
                BaseColumns.COLUMN_ALBUM_ID +
                " INTEGER," +
                LikedColumns.COLUMN_LIKED_TIMES +
                " INTEGER" +
                LikedColumns.COLUMN_ADDED_WHEN +
                " LONG)";
        db.execSQL(liked);

        String recent = "CREATE TABLE "
                + RecentColumns.TABLE_NAME
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + BaseColumns.COLUMN_TITLE +
                " TEXT," +
                BaseColumns.COLUMN_ARTIST +
                " TEXT," +
                BaseColumns.COLUMN_ALBUM +
                " TEXT," +
                BaseColumns.COLUMN_DURATION +
                " LONG," +
                BaseColumns.COLUMN_URL +
                " TEXT," +
                BaseColumns.COLUMN_SONG_ID +
                " INTEGER," +
                BaseColumns.COLUMN_ALBUM_ID +
                " INTEGER," +
                RecentColumns.COLUMN_ADDED_WHEN +
                " LONG)";
        db.execSQL(recent);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Noop
    }

    public interface BaseColumns {
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_SONG_ID = "song_id";
        public static final String COLUMN_ALBUM_ID = "album_id";
    }


    public interface LikedColumns extends BaseColumns {
        public static final String TABLE_NAME = "liked";
        public static final String COLUMN_ADDED_WHEN = "added_when";
        public static final String COLUMN_LIKED_TIMES = "liked_times";
    }

    public interface RecentColumns extends BaseColumns {
        public static final String TABLE_NAME = "recent";
        public static final String COLUMN_ADDED_WHEN = "added_when";
    }

}
