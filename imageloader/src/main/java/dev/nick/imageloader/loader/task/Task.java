package dev.nick.imageloader.loader.task;

public interface Task<T extends TaskRecord> extends Runnable {
    T getTaskRecord();
}
