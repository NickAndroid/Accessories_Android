package dev.nick.imageloader;

import java.util.concurrent.atomic.AtomicInteger;

class LoaderFactory {
    private static AtomicInteger sLoaderId = new AtomicInteger(0);

    static int assignLoaderId() {
        return sLoaderId.getAndIncrement();
    }
}
