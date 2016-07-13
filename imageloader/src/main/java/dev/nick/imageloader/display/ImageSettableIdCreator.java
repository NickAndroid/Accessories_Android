package dev.nick.imageloader.display;

import android.support.annotation.NonNull;

public interface ImageSettableIdCreator {
    int createSettableId(@NonNull ImageSettable settable);
}
