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

package dev.nick.accessories.injection.processors;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import dev.nick.accessories.injection.annotation.binding.BindHandler;

class HandlerProcessor extends FieldProcessor {

    public HandlerProcessor(Context appContext) {
        super(appContext);
    }

    @Override
    protected Object parseField(Field field) {
        BindHandler anno = field.getAnnotation(BindHandler.class);
        boolean worker = anno.workerThread();
        Looper looper = Looper.getMainLooper();
        if (worker) {
            HandlerThread handlerThread = new HandlerThread("HandlerThread");
            handlerThread.start();
            looper = handlerThread.getLooper();
        }
        return new android.os.Handler(looper);
    }

    @NonNull
    @Override
    public Class<? extends Annotation> targetAnnotation() {
        return BindHandler.class;
    }
}
