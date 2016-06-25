package dev.nick.imageloader.display;

import android.support.annotation.IdRes;

public class DisplayOption {

    @IdRes
    int imgResShowWhenError;
    @IdRes
    int imgResShowWhenLoading;

    public DisplayOption(int imgResShowWhenError, int imgResShowWhenLoading) {
        this.imgResShowWhenError = imgResShowWhenError;
        this.imgResShowWhenLoading = imgResShowWhenLoading;
    }

    public int getImgResShowWhenError() {
        return imgResShowWhenError;
    }

    public int getImgResShowWhenLoading() {
        return imgResShowWhenLoading;
    }
}
