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

package dev.nick.imageloader.loader.task;

import android.support.annotation.Nullable;

import java.util.concurrent.FutureTask;

import dev.nick.imageloader.logger.LoggerManager;

public class FutureImageTask extends FutureTask<Void> {

    private DoneListener mListener;
    private DisplayTask mTask;

    private boolean mCancelOthersBeforeRun;

    public FutureImageTask(DisplayTask task, @Nullable DoneListener listener, boolean cancelOthersBeforeRun) {
        super(task);
        this.mTask = task;
        this.mListener = listener;
        this.mCancelOthersBeforeRun = cancelOthersBeforeRun;
    }

    @Override
    protected void done() {
        super.done();
        LoggerManager.getLogger(getClass()).funcEnter();
        if (mListener != null) mListener.onDone(this);
    }

    public interface DoneListener {
        void onDone(FutureImageTask futureImageTask);
    }

    public boolean shouldCancelOthersBeforeRun() {
        return mCancelOthersBeforeRun;
    }

    public DisplayTask getListenableTask() {
        return mTask;
    }
}
