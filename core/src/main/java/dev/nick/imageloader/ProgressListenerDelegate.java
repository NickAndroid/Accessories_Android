package dev.nick.imageloader;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.debug.Logger;
import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.task.DisplayTaskRecord;
import dev.nick.imageloader.worker.task.TaskManager;

abstract class ProgressListenerDelegate<T> implements ProgressListener<T> {

    protected TaskManager taskManager;
    protected CacheManager<T> cacheManager;

    protected ProgressListener<T> listener;

    protected ImageSeat<T> settable;
    protected String url;
    protected DisplayOption<T> option;
    protected DimenSpec dimenSpec;
    protected Boolean canceled = Boolean.FALSE;
    protected Boolean isTaskDirty = null;
    protected Logger mLogger;
    private DisplayTaskRecord taskRecord;

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
        this.mLogger = LoggerManager.getLogger(getClass());
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
        mLogger.verbose("isTaskDirty: " + isTaskDirty);
        return isTaskDirty;
    }
}
