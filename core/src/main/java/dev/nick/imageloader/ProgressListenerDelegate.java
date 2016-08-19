package dev.nick.imageloader;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.task.DisplayTaskRecord;
import dev.nick.imageloader.worker.task.TaskManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;

abstract class ProgressListenerDelegate<T> implements ProgressListener<T> {

    protected TaskManager taskManager;
    protected CacheManager<T> cacheManager;

    protected ProgressListener<T> listener;

    protected ImageSeat<T> settable;
    protected String url;
    protected DisplayOption<T> option;
    protected DimenSpec dimenSpec;

    private DisplayTaskRecord taskRecord;

    protected Boolean canceled = Boolean.FALSE;
    protected Boolean isTaskDirty = null;

    public ProgressListenerDelegate(
            CacheManager<T> cacheManager,
            TaskManager taskManager,
            ProgressListener<T> listener,
            DimenSpec dimenSpec,
            DisplayOption<T> option,
            ImageSeat<T> imageSeat,
            DisplayTaskRecord taskRecord,
            String url) {
        this.cacheManager = cacheManager;
        this.taskManager = taskManager;
        this.dimenSpec = dimenSpec;
        this.listener = listener;
        this.option = option;
        this.settable = imageSeat;
        this.taskRecord = taskRecord;
        this.url = url;
    }

    @Override
    public void onStartLoading() {
        if (!canceled && !checkTaskDirty()) {
            UIThreadRouter.getSharedRouter().callOnStart(listener);
        }
    }

    @Override
    public void onProgressUpdate(float progress) {
        if (!canceled && !checkTaskDirty()) {
            UIThreadRouter.getSharedRouter().callOnProgressUpdate(listener, progress);
        }
    }

    @Override
    public void onCancel() {
        canceled = Boolean.TRUE;
        if (!checkTaskDirty()) {
            UIThreadRouter.getSharedRouter().callOnCancel(listener);
        }
    }

    protected synchronized boolean checkTaskDirty() {
        if (isTaskDirty == null || !isTaskDirty) {
            isTaskDirty = taskManager.interruptDisplay(taskRecord);
        }
        return isTaskDirty;
    }
}
