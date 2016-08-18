package dev.nick.imageloader;

import android.graphics.Bitmap;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.task.DisplayTaskRecord;
import dev.nick.imageloader.loader.task.TaskManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageSeat;

abstract class ProgressListenerDelegate<T> implements ProgressListener<T> {

    protected TaskManager taskManager;
    protected CacheManager<T> cacheManager;

    protected ProgressListener<T> listener;

    protected ImageSeat<Bitmap> settable;
    protected String url;
    protected DisplayOption<T> option;
    protected ViewSpec viewSpec;

    private DisplayTaskRecord taskRecord;

    protected Boolean canceled = Boolean.FALSE;
    protected Boolean isTaskDirty = null;

    public ProgressListenerDelegate(
            CacheManager<T> cacheManager,
            TaskManager taskManager,
            ProgressListener<T> listener,
            ViewSpec viewSpec,
            DisplayOption<T> option,
            ImageSeat<Bitmap> imageSeat,
            DisplayTaskRecord taskRecord,
            String url) {
        this.cacheManager = cacheManager;
        this.taskManager = taskManager;
        this.viewSpec = viewSpec;
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
