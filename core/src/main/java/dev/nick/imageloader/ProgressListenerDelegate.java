/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.imageloader;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.MediaChair;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.task.DisplayTaskRecord;
import dev.nick.imageloader.worker.task.TaskManager;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

abstract class ProgressListenerDelegate<T> implements ProgressListener<T> {

    protected TaskManager taskManager;
    protected CacheManager<T> cacheManager;

    protected ProgressListener<T> listener;

    protected MediaChair<T> settable;
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
            MediaChair<T> mediaChair,
            DisplayTaskRecord taskRecord,
            String url) {
        this.cacheManager = cacheManager;
        this.taskManager = taskManager;
        this.dimenSpec = dimenSpec;
        this.listener = listener;
        this.option = option;
        this.settable = mediaChair;
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

    protected abstract void callOnComplete(T result);

    protected boolean checkTaskDirty() {
        synchronized (this) {
            if (isTaskDirty == null || !isTaskDirty) {
                isTaskDirty = taskManager.interruptDisplay(taskRecord);
            }
        }
        return isTaskDirty;
    }
}
