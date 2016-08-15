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

import dev.nick.imageloader.logger.LoggerManager;

public final class RequestQueueManager<T> implements RequestHandler<T> {

    RequestQueue<T> mQueue;
    RequestHandler<T> mRequestHandler;
    String mName;

    private RequestQueueManager(RequestHandler<T> requestHandler, IdleStateMonitor idleStateMonitor, QueuePolicy policy, String name) {
        mQueue = new RequestQueue<>();
        mQueue.setStateMonitor(idleStateMonitor);
        mQueue.setPolicy(policy);
        mRequestHandler = requestHandler;
        mName = name;
    }

    public static <T> RequestQueueManager<T> createStarted(RequestHandler<T> requestHandler) {
        return createStarted(requestHandler, null);
    }

    public static <T> RequestQueueManager<T> createStarted(RequestHandler<T> requestHandler, IdleStateMonitor idleStateMonitor) {
        return createStarted(requestHandler, idleStateMonitor, QueuePolicy.FIFO, null);
    }

    public static <T> RequestQueueManager<T> createStarted(RequestHandler<T> requestHandler, IdleStateMonitor idleStateMonitor, QueuePolicy policy, String name) {
        return new RequestQueueManager<T>(requestHandler, idleStateMonitor, policy, name).loop();
    }

    RequestQueueManager<T> loop() {
        Looper<T> looper = new Looper<>(this, mQueue, mName);
        looper.startLoop();
        return this;
    }

    public void terminate() {
        LoggerManager.getLogger(getClass()).funcEnter();
        mQueue.deactivate();
    }

    public void push(T request) {
        mQueue.add(request);
    }

    @Override
    public boolean handleRequest(T request) {
        return mRequestHandler.handleRequest(request);
    }
}
