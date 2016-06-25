package dev.nick.imageloader.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;

public class FileImageFetcher extends BaseImageFetcher {

    public FileImageFetcher(PathSplitter<String> splitter) {
        super(splitter);
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception{
        String path = splitter.getRealPath(url);
        File file = new File(path);
        if (!file.exists()) {
            logW("File: " + url + " do NOT exist.");
            throw new FileNotFoundException("File:" + url);
        }
        return BitmapFactory.decodeFile(path);
    }
}
