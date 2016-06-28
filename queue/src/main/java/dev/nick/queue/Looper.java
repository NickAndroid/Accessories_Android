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

package dev.nick.queue;

public class Looper<T> implements Runnable {

    MessageStack<T> stack;
    MessageHandler<T> messageHandler;

    public Looper(MessageHandler<T> messageHandler, MessageStack<T> stack) {
        this.messageHandler = messageHandler;
        this.stack = stack;
    }

    void startLoop() {
        new Thread(this).start();
    }

    void loop() {
        for (; ; ) {
            final T t = stack.take();
            if (t == null) break;
            messageHandler.handleMessage(t);
        }
    }

    @Override
    public void run() {
        loop();
    }
}
