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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.common.SharedExecutor;
import dev.nick.accessoriestest.model.IMediaTrack;
import dev.nick.accessoriestest.service.UserCategory;

public class TrackLoader {

    private static TrackLoader sLoader;

    private TrackLoader() {
        // Noop.
    }

    public static synchronized TrackLoader get() {
        if (sLoader == null) sLoader = new TrackLoader();
        return sLoader;
    }

    public void loadAsync(final UserCategory category, final Listener listener, final Context context) {

        SharedExecutor.get().execute(new Runnable() {
            @Override
            public void run() {
                listener.postLoading(category);
                listener.postLoaded(category, load(category, context));
            }
        });
    }

    public List<IMediaTrack> load(UserCategory category, Context context) {
        if (category == UserCategory.ALL) return loadAll(context);
        if (category == UserCategory.RECENT) return loadRecent(context);
        throw new IllegalArgumentException("Bad category #" + category);
    }

    private List<IMediaTrack> loadRecent(Context c) {
        List<IMediaTrack> list = new ArrayList<IMediaTrack>();
        DatabaseHelper databaseHelper = new DatabaseHelper(c);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = db.query(UserCategory.RECENT.name(), null, null, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                long id = cursor
                        .getLong(cursor.getColumnIndex(DatabaseHelper.BaseColumns.COLUMN_SONG_ID));
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(DatabaseHelper.BaseColumns.COLUMN_TITLE));

                String singer = cursor.getString(cursor
                        .getColumnIndexOrThrow(DatabaseHelper.BaseColumns.COLUMN_ARTIST));

                int time = cursor.getInt(cursor
                        .getColumnIndexOrThrow(DatabaseHelper.BaseColumns.COLUMN_DURATION));

                String name = cursor.getString(cursor
                        .getColumnIndexOrThrow(DatabaseHelper.BaseColumns.COLUMN_TITLE));
                String url = cursor.getString(cursor
                        .getColumnIndexOrThrow(DatabaseHelper.BaseColumns.COLUMN_URL));
                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(DatabaseHelper.BaseColumns.COLUMN_ALBUM));
                long albumid = cursor.getLong(cursor
                        .getColumnIndex(DatabaseHelper.BaseColumns.COLUMN_ALBUM_ID));

                IMediaTrack track = new IMediaTrack();
                track.setTitle(title);
                track.setArtist(singer);
                track.setId(id);
                track.setUrl(url);
                track.setAlbumId(albumid);
                track.setAlbum(album);
                track.setDuration(time);
                list.add(track);
            }
        }
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            // Noop.
        }
        return list;
    }

    private List<IMediaTrack> loadAll(Context c) {
        List<IMediaTrack> list = new ArrayList<IMediaTrack>();
        ContentResolver cr = c.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                long id = cursor
                        .getLong(cursor.getColumnIndex(BaseColumns._ID));
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));

                String singer = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));

                int time = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));

                String name = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                //
                // String suffix = name
                // .substring(name.length() - 4, name.length());

                String url = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
                long albumid = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));

                IMediaTrack track = new IMediaTrack();
                track.setTitle(title);
                track.setArtist(singer);
                track.setId(id);
                track.setUrl(url);
                track.setAlbumId(albumid);
                track.setAlbum(album);
                track.setDuration(time);
                list.add(track);
            }
        }
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            // Noop.
        }
        return list;
    }


    public static abstract class Listener {

        private Handler handler;

        public Listener() {
            handler = new Handler();
        }

        void postLoading(final UserCategory category) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onLoading(category);
                }
            });
        }

        void postLoaded(final UserCategory category, final List<IMediaTrack> tracks) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onLoaded(category, tracks);
                }
            });
        }

        public abstract void onLoading(UserCategory category);

        public abstract void onLoaded(UserCategory category, List<IMediaTrack> tracks);

    }

}
