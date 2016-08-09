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

package dev.nick.imageloader.queue;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import dev.nick.imageloader.logger.LoggerManager;

class RequestQueue<T>{

    static final int IDLE_TIME_SECONDS = 24;

    Map<Priority, LinkedBlockingDeque<T>> mHolders;

    IdleStateMonitor mIdleStateMonitor;

    QueuePolicy mPolicy = QueuePolicy.FIFO;

    boolean mActive = true;
    boolean mIdleSignal = true;

    Priority mPrioritySignal = Priority.HIGH;

    public RequestQueue() {
        mHolders = Maps.newEnumMap(Priority.class);
    }

    public void setStateMonitor(IdleStateMonitor monitor) {
        this.mIdleStateMonitor = monitor;
    }

    public void setPolicy(QueuePolicy policy) {
        this.mPolicy = policy;
    }

    public T add(T item, Priority priority) {
        return addByPriority(item, priority);
    }

    private T addByPriority(T item, Priority priority) {
        if (!mActive) return null;
        if (mHolders.get(priority).add(item)) {
            signalIdle();
            if (priority.sequencer.isHigherThan(mPrioritySignal)) {
                // We have request with higher priority.
                mPrioritySignal = priority;
            }
            return item;
        }
        return null;
    }

    public T offer(T item, Priority priority) {
        return addByPriority(item, priority);
    }

    private T offerByPriority(T item, Priority priority) {
        if (!mActive) return null;
        if (mHolders.get(priority).offer(item)) {
            signalIdle();
            if (priority.sequencer.isHigherThan(mPrioritySignal)) {
                // We have request with higher priority.
                mPrioritySignal = priority;
            }
            return item;
        }
        return null;
    }

    public T next() {
        if (!mActive) return null;
        T polled = pollFromCurrentSignal();
        if (polled == null) {
            onIdle();
            return next();
        } else {
            return polled;
        }
    }

    private T pollFromCurrentSignal() {
        do {
            T t = pollByPriority(mPrioritySignal);
            if (t != null) {
                LoggerManager.getLogger(getClass()).info("Polled from:" + mPrioritySignal);
                return t;
            }
        } while ((mPrioritySignal = mPrioritySignal.sequencer.lower()) != null);
        return pollFromCurrentSignal();
    }

    private T pollByPriority(Priority priority) {
        LinkedBlockingDeque<T> deque = mHolders.get(priority);
        long timeout = priority.timeoutMillSec;
        try {
            return mPolicy == QueuePolicy.FIFO ? deque.pollFirst(timeout, TimeUnit.MILLISECONDS)
                    : deque.pollFirst(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void deactivate() {
        mActive = false;
        unSignalIdle();
    }

    private void signalIdle() {
        mIdleSignal = true;
    }

    private void unSignalIdle() {
        mIdleSignal = false;
    }

    protected void onIdle() {
        if (mIdleSignal && mIdleStateMonitor != null) {
            mIdleStateMonitor.onIdle();
            mIdleSignal = false;
        }
    }
}
