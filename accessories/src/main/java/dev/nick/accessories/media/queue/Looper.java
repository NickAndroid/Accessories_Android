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

class Looper<T> implements Runnable {

    RequestQueue<T> stack;
    RequestHandler<T> requestHandler;
    String name;

    public Looper(RequestHandler<T> requestHandler, RequestQueue<T> stack, String name) {
        this.requestHandler = requestHandler;
        this.stack = stack;
        this.name = name;
    }

    void startLoop() {
        new Thread(this, name).start();
    }

    void loop() {
        for (; ; ) {
            final T t = stack.next();
            if (t == null) break;
            requestHandler.handleRequest(t);
        }
    }

    @Override
    public void run() {
        loop();
    }
}
