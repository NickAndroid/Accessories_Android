package dev.nick.imageloader.worker.task;

public abstract class BaseDisplayTask<T> implements DisplayTask<T> {
    @Override
    public DisplayTaskRecord getTaskRecord() {
        return null;
    }
}
