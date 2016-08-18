package dev.nick.imageloader.worker.task;


public interface TaskInterrupter {
    boolean interruptDisplay(DisplayTaskRecord record);
    boolean interruptExecute(DisplayTaskRecord record);
}
