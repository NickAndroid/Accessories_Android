package dev.nick.imageloader.loader.task;


public interface TaskInterrupter {
    boolean interruptDisplay(DisplayTaskRecord record);
    boolean interruptExecute(DisplayTaskRecord record);
}
