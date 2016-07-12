package dev.nick.imageloader.loader.task;

import android.support.annotation.NonNull;

public interface TaskMonitor {
    boolean shouldRun(@NonNull ImageTask task);
}
