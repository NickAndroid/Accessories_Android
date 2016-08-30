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

package dev.nick.accessories.binding.processors;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.reflect.Type;

import dev.nick.accessories.binding.annotation.common.Component;

public abstract class TypeProcessor extends BaseProcessor<Object, Type> {

    public TypeProcessor(Context appContext) {
        super(appContext);
    }

    @NonNull
    @Override
    public Component.Android scope() {
        return Component.Android.NoLimit;
    }
}
