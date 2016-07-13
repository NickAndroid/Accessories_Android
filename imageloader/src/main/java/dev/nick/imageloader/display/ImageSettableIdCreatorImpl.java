package dev.nick.imageloader.display;

import android.support.annotation.NonNull;

public class ImageSettableIdCreatorImpl implements ImageSettableIdCreator {
    @Override
    public int createSettableId(@NonNull ImageSettable settable) {
        return settable.hashCode();
    }
}
