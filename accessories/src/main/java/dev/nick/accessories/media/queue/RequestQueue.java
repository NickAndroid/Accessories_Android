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

package dev.nick.accessories.media.queue;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

class RequestQueue<T> {

    static final int IDLE_TIME_SECONDS = 24;

    LinkedBlockingDeque<T> mHolder;

    IdleStateMonitor mIdleStateMonitor;

    QueuePolicy mPolicy = QueuePolicy.FIFO;

    boolean mActive = true;
    boolean mIdleSignal = true;

    public RequestQueue() {
        mHolder = new LinkedBlockingDeque<>();
    }

    public void setStateMonitor(IdleStateMonitor monitor) {
        this.mIdleStateMonitor = monitor;
    }

    public void setPolicy(QueuePolicy policy) {
        this.mPolicy = policy;
    }

    public T add(T item) {
        if (!mActive) return null;
        if (mHolder.add(item)) {
            signal();
        }
        return item;
    }

    public T next() {
        if (!mActive) return null;
        try {
            T polled = mPolicy == QueuePolicy.FIFO ? mHolder.pollFirst(IDLE_TIME_SECONDS, TimeUnit.SECONDS)
                    : mHolder.pollFirst(IDLE_TIME_SECONDS, TimeUnit.SECONDS);
            if (polled == null) {
                onIdle();
                return next();
            } else {
                return polled;
            }
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void deactivate() {
        mActive = false;
        unSignal();
    }

    private void signal() {
        mIdleSignal = true;
    }

    private void unSignal() {
        mIdleSignal = false;
    }

    protected void onIdle() {
        if (mIdleSignal && mIdleStateMonitor != null) {
            mIdleStateMonitor.onIdle();
            mIdleSignal = false;
        }
    }
}
