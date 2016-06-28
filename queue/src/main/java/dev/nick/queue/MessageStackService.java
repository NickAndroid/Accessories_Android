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

public final class MessageStackService<T> implements MessageHandler<T> {

    MessageStack<T> mStack;
    MessageHandler<T> mMessageHandler;

    private MessageStackService(MessageHandler<T> messageHandler) {
        mStack = new MessageStack<>();
        mMessageHandler = messageHandler;
    }

    public static <T> MessageStackService<T> createStarted(MessageHandler<T> messageHandler) {
        return new MessageStackService<T>(messageHandler).loop();
    }

    MessageStackService<T> loop() {
        Looper<T> looper = new Looper<>(this, mStack);
        looper.startLoop();
        return this;
    }

    public void push(T message) {
        mStack.push(message);
    }

    @Override
    public boolean handleMessage(T message) {
        return mMessageHandler.handleMessage(message);
    }
}
