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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import dev.nick.accessoriestest.model.IMediaTrack;
import dev.nick.accessoriestest.service.UserCategory;

public class TrackManagerRepoImpl implements TrackManagerRepo {

    private static TrackManagerRepoImpl sImpl;
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;

    private TrackManagerRepoImpl(Context c) {
        this.mContext = c;
        this.mDatabaseHelper = new DatabaseHelper(c);
    }

    public static synchronized TrackManagerRepoImpl from(Context context) {
        if (sImpl == null) sImpl = new TrackManagerRepoImpl(context);
        return sImpl;
    }

    @Override
    public List<IMediaTrack> findBy(UserCategory category) {
        return TrackLoader.get().load(category, mContext);
    }

    @Override
    public void addTo(UserCategory category, IMediaTrack track) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.BaseColumns.COLUMN_URL, track.getUrl());
        v.put(DatabaseHelper.BaseColumns.COLUMN_ALBUM, track.getAlbum());
        v.put(DatabaseHelper.BaseColumns.COLUMN_ALBUM_ID, track.getAlbumId());
        v.put(DatabaseHelper.BaseColumns.COLUMN_ARTIST, track.getArtist());
        v.put(DatabaseHelper.BaseColumns.COLUMN_DURATION, track.getDuration());
        v.put(DatabaseHelper.BaseColumns.COLUMN_SONG_ID, track.getId());
        v.put(DatabaseHelper.BaseColumns.COLUMN_TITLE, track.getTitle());
        db.insert(category.name(), null, v);
    }

    @Override
    public void removeFrom(UserCategory category, IMediaTrack track) {

    }
}
