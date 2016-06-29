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

package dev.nick.stack;

import java.util.Stack;
import java.util.concurrent.CountDownLatch;

class RequestStack<T> {

    Stack<T> mHolder;

    CountDownLatch mSignal;

    boolean mActive = true;

    public RequestStack() {
        mHolder = new Stack<>();
    }

    public T push(T item) {
        T t = mHolder.push(item);
        if (t != null)
            signal();
        return t;
    }

    public T take() {
        if (!mActive) return null;
        if (empty()) {
            waitForSignal(1);
        }
        return pop();
    }

    public void deactivate() {
        mActive = false;
    }

    private void waitForSignal(int expected) {
        mSignal = new CountDownLatch(expected);
        while (true) {
            try {
                mSignal.await();
                break;
            } catch (InterruptedException e) {
                // Ignored.
            }
        }
    }

    private void signal() {
        if (mSignal != null && mSignal.getCount() > 0) {
            mSignal.countDown();
        }
    }

    public boolean empty() {
        return mHolder.empty();
    }

    public T peek() {
        return mHolder.peek();
    }

    public T pop() {
        return mHolder.pop();
    }

    public int search(Object o) {
        return mHolder.search(o);
    }
}
