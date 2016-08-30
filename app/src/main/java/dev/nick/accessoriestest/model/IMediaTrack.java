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

package dev.nick.accessoriestest.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nick on 16-2-7.
 * Email: nick.guo.dev@icloud.com
 * Github: https://github.com/NickAndroid
 */
public class IMediaTrack implements Parcelable {

    public static final Creator<IMediaTrack> CREATOR = new Creator<IMediaTrack>() {
        @Override
        public IMediaTrack createFromParcel(Parcel in) {
            return new IMediaTrack(in);
        }

        @Override
        public IMediaTrack[] newArray(int size) {
            return new IMediaTrack[size];
        }
    };
    private String title;
    private String artist;
    private long id;
    private long albumId;
    private long duration;
    private String url;
    private String album;

    public IMediaTrack() {
    }

    protected IMediaTrack(Parcel in) {
        title = in.readString();
        artist = in.readString();
        id = in.readLong();
        albumId = in.readLong();
        url = in.readString();
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeLong(id);
        dest.writeLong(albumId);
        dest.writeString(url);
    }

    @Override
    public String toString() {
        return "IMediaTrack{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", id=" + id +
                ", albumId=" + albumId +
                ", url='" + url + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                '}';
    }
}
