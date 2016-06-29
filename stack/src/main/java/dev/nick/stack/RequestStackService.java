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

public final class RequestStackService<T> implements RequestHandler<T> {

    RequestStack<T> mStack;
    RequestHandler<T> mRequestHandler;

    private RequestStackService(RequestHandler<T> requestHandler) {
        mStack = new RequestStack<>();
        mRequestHandler = requestHandler;
    }

    public static <T> RequestStackService<T> createStarted(RequestHandler<T> requestHandler) {
        return new RequestStackService<T>(requestHandler).loop();
    }

    RequestStackService<T> loop() {
        Looper<T> looper = new Looper<>(this, mStack);
        looper.startLoop();
        return this;
    }

    public void push(T request) {
        mStack.push(request);
    }

    @Override
    public boolean handle(T request) {
        return mRequestHandler.handle(request);
    }
}
