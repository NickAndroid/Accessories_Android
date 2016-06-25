package dev.nick.imageloader.loader;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

public class ContentImageFetcher extends BaseImageFetcher {

    @NonNull
    ImageFetcher fileImageFetcher;

    public ContentImageFetcher(PathSplitter<String> splitter, @NonNull ImageFetcher fileImageFetcher) {
        super(splitter);
        this.fileImageFetcher = fileImageFetcher;
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url, ImageInfo info) throws Exception{

        Uri uri = Uri.parse(url);

        String[] pro = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(uri, pro, null, null, null);

        if (cursor == null) {
            logW("No cursor found for url:" + url);
            return null;
        }

        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String filePath = cursor.getString(index);

        cursor.close();

        return fileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + filePath, info);
    }
}
