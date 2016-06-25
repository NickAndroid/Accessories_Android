package dev.nick.twenty;

import com.nick.scalpel.ScalpelApplication;

import dev.nick.eventbus.EventBus;
import dev.nick.imageloader.ZImageLoader;

public class TwentyApp extends ScalpelApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.create(this);
        ZImageLoader.init(this);
    }
}
