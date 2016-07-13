package dev.nick.imageloader.loader.task;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskManagerImpl implements TaskManager {

    private final AtomicInteger mTaskId = new AtomicInteger(0);

    @Override
    public int nextTaskId() {
        return mTaskId.getAndIncrement();
    }
}
