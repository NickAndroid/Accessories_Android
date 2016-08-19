package dev.nick.imageloader.cache;

import android.content.Context;
import android.graphics.Movie;
import android.support.annotation.NonNull;

public class MovieCacheManager implements CacheManager<Movie> {

    public MovieCacheManager(CachePolicy cachePolicy, Context context) {

    }

    @Override
    public Movie get(String url) {
        return null;
    }

    @Override
    public String getCachePath(String url) {
        return null;
    }

    @Override
    public boolean cache(@NonNull String url, @NonNull Movie value) {
        return false;
    }

    @Override
    public boolean isDiskCacheEnabled() {
        return false;
    }

    @Override
    public boolean isMemCacheEnabled() {
        return false;
    }

    @Override
    public void evictDisk() {

    }

    @Override
    public void evictMem() {

    }
}
