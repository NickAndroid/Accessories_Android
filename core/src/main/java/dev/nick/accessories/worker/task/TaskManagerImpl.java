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

package dev.nick.accessories.worker.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nick.accessories.control.LoaderState;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class TaskManagerImpl implements TaskManager {

    private final AtomicInteger mTaskId = new AtomicInteger(0);

    private final Map<Long, DisplayTaskRecord> mTaskLockMap;

    private long mClearTaskRequestedTimeMills;

    private Logger mLogger;

    private LoaderState mState = LoaderState.RUNNING;

    public TaskManagerImpl() {
        this.mTaskLockMap = new HashMap<>();
        this.mLogger = LoggerManager.getLogger(getClass());
    }

    @Override
    public int nextTaskId() {
        return mTaskId.getAndIncrement();
    }

    @Override
    public void clearTasks() {
        mClearTaskRequestedTimeMills = System.currentTimeMillis();
    }

    @Override
    public void onDisplayTaskCreated(DisplayTaskRecord record) {
        int taskId = record.getTaskId();
        long settableId = record.getSettableId();
        synchronized (mTaskLockMap) {
            DisplayTaskRecord exists = mTaskLockMap.get(settableId);
            if (exists != null) {
                exists.setTaskId(taskId);
            } else {
                mTaskLockMap.put(settableId, record);
            }
        }
    }

    @Override
    public boolean interruptDisplay(DisplayTaskRecord record) {
        return isTaskDirty(record);
    }

    @Override
    public boolean interruptExecute(DisplayTaskRecord record) {
        return isTaskDirty(record);
    }

    private boolean isTaskDirty(DisplayTaskRecord task) {

        if (mState == LoaderState.TERMINATED) {
            mLogger.debug("Mark as dirty when terminated");
            return true;
        }

        boolean outDated = task.upTime() <= mClearTaskRequestedTimeMills;

        if (outDated) {
            mLogger.verbose("Mark as dirty when outDated.");
            return true;
        }

        synchronized (mTaskLockMap) {
            DisplayTaskRecord lock = mTaskLockMap.get(task.getSettableId());
            if (lock != null) {
                int taskId = lock.getTaskId();
                // We have new task to load for this settle.
                if (taskId > task.getTaskId()) {
                    mLogger.verbose("Mark as dirty when found new same task");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized void terminate() {
        synchronized (mTaskLockMap) {
            mTaskLockMap.clear();
        }
        mState = LoaderState.TERMINATED;
    }
}
