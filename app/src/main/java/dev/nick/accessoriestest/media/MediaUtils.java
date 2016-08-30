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

package dev.nick.accessoriestest.media;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class MediaUtils {

    /**
     * @param c
     * @return get all the tracks frm android
     */
    public static List<Track> getTrackList(Context c) {

        List<Track> list = new ArrayList<Track>();
        ContentResolver cr = c.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                long id = cursor
                        .getLong(cursor.getColumnIndex(BaseColumns._ID));
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaColumns.TITLE));

                String singer = cursor.getString(cursor
                        .getColumnIndexOrThrow(AudioColumns.ARTIST));

                int time = cursor.getInt(cursor
                        .getColumnIndexOrThrow(AudioColumns.DURATION));
                time = time / 60000;

                String name = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME));
                //
                // String suffix = name
                // .substring(name.length() - 4, name.length());

                String url = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaColumns.DATA));
                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(AudioColumns.ALBUM));
                long albumid = cursor.getLong(cursor
                        .getColumnIndex(AudioColumns.ALBUM_ID));

                if (url.endsWith(".mp3") || url.endsWith(".MP3")) {
                    Track track = new Track();
                    track.setTitle(title);
                    track.setArtist(singer);
                    track.setId(id);
                    track.setUrl(url);
                    track.setAlbumId(albumid);
                    list.add(track);
                }
            }
        }
        // avoid null point
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        }
        return list;

    }

}