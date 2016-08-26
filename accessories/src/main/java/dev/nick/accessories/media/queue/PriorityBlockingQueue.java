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

import android.support.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import dev.nick.accessories.logger.LoggerManager;

public class PriorityBlockingQueue<E> extends LinkedBlockingDeque<E> implements BlockingQueue<E> {

    @Override
    public boolean add(E e) {
        if (e instanceof PriorityRemarkable) {
            PriorityRemarkable remarkable = (PriorityRemarkable) e;
            Priority priority = remarkable.getRemark();
            switch (priority) {
                case HIGH:
                    super.addFirst(e);
                    LoggerManager.getLogger(getClass()).warn("add for High");
                    return true;
                case LOW:
                    super.addLast(e);
                    LoggerManager.getLogger(getClass()).warn("add for low");
                    return true;
                default:
                    break;
            }
        }

        return super.add(e);
    }

    @Override
    public boolean offer(@NonNull E e) {
        if (e instanceof PriorityRemarkable) {
            PriorityRemarkable remarkable = (PriorityRemarkable) e;
            Priority priority = remarkable.getRemark();
            switch (priority) {
                case HIGH:
                    LoggerManager.getLogger(getClass()).warn("offer for High");
                    return super.offerFirst(e);
                case LOW:
                    LoggerManager.getLogger(getClass()).warn("offer for Low");
                    return super.offerLast(e);
                default:
                    break;
            }
        }
        return super.offer(e);
    }

    @Override
    public boolean offer(E e, long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        if (e instanceof PriorityRemarkable) {
            PriorityRemarkable remarkable = (PriorityRemarkable) e;
            Priority priority = remarkable.getRemark();
            switch (priority) {
                case HIGH:
                    LoggerManager.getLogger(getClass()).warn("offer for High");
                    return super.offerFirst(e, timeout, unit);
                case LOW:
                    LoggerManager.getLogger(getClass()).warn("offer for Low");
                    return super.offerLast(e, timeout, unit);
                default:
                    break;
            }
        }
        return super.offer(e, timeout, unit);
    }
}
