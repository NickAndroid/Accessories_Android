package dev.nick.imageloader.loader;

import dev.nick.imageloader.display.ImageQuality;

public class DecodeSpec {

    ImageQuality quality;
    ViewSpec viewSpec;

    public DecodeSpec(ImageQuality quality, ViewSpec viewSpec) {
        this.quality = quality;
        this.viewSpec = viewSpec;
    }

    public ImageQuality getQuality() {
        return quality;
    }

    public ViewSpec getViewSpec() {
        return viewSpec;
    }
}
