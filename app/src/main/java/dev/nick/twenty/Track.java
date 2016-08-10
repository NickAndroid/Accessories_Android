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

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * @author Nick
 */
public class Track implements Serializable {

    private static final long serialVersionUID = 10090099L;
    private String title;
    private Bitmap art;
    private String artist;
    private long id;
    private long albumId;
    private String url;

    /**
     * @return the albumId
     */
    public long getAlbumId() {
        return albumId;
    }

    /**
     * @param albumId the albumId to set
     */
    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    /**
     * @return the from
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the from to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the art
     */
    public Bitmap getArt() {
        return art;
    }

    /**
     * @param art the art to set
     */
    public void setArt(Bitmap art) {
        this.art = art;
    }

    /**
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist the artist to set
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

}
