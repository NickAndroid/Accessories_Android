package dev.nick.accessories.worker.task;

import java.util.concurrent.Callable;

public interface Task<T extends TaskRecord, X> extends Callable<X>, Runnable {
    T getTaskRecord();
}
