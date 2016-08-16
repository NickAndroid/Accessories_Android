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

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.concurrent.FutureTask;

import dev.nick.imageloader.loader.result.Result;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.queue.PriorityRemarkable;

public class FutureImageTask extends FutureTask<Result<Bitmap>> implements PriorityRemarkable {

    private TaskActionListener mListener;
    private DisplayTask<Bitmap> mTask;

    private Priority mPriority;

    private boolean mCancelOthersBeforeRun;

    public FutureImageTask(DisplayTask<Bitmap> task, @Nullable TaskActionListener listener, boolean cancelOthersBeforeRun) {
        super(task);
        this.mTask = task;
        this.mListener = listener;
        this.mPriority = Priority.NORMAL;
        this.mCancelOthersBeforeRun = cancelOthersBeforeRun;
    }

    @Override
    protected void done() {
        super.done();
        if (mListener != null) mListener.onDone(this);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = super.cancel(mayInterruptIfRunning);
        if (mListener != null) mListener.onCancel(this);
        return result;
    }

    public void setPriority(Priority priority) {
        this.mPriority = priority;
    }

    @Override
    public String toString() {
        return "FutureImageTask{" +
                "mListener=" + mListener +
                ", mTask=" + mTask +
                ", mCancelOthersBeforeRun=" + mCancelOthersBeforeRun +
                '}';
    }

    public boolean shouldCancelOthersBeforeRun() {
        return mCancelOthersBeforeRun;
    }

    public DisplayTask<Bitmap> getListenableTask() {
        return mTask;
    }

    @Override
    public Priority getRemark() {
        return mPriority;
    }

    public interface TaskActionListener {
        void onDone(FutureImageTask futureImageTask);

        void onCancel(FutureImageTask futureImageTask);
    }
}
