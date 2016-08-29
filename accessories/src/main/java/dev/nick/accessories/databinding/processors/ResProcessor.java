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

package dev.nick.accessories.databinding.processors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.guava.base.Preconditions;

import java.lang.reflect.Field;

import dev.nick.accessories.databinding.annotation.common.Component;

import static dev.nick.accessories.databinding.utils.ReflectionUtils.getField;
import static dev.nick.accessories.databinding.utils.ReflectionUtils.makeAccessible;
import static dev.nick.accessories.databinding.utils.ReflectionUtils.setField;

public abstract class ResProcessor extends BaseProcessor<Object, Field> {

    public ResProcessor(Context appContext) {
        super(appContext);
    }

    @Override
    public boolean process(@Nullable Object container, @NonNull Field field) {
        in();
        makeAccessible(field);
        Object fieldObject = getField(field, container);
        if (fieldObject != null) {
            report("Ignored for none null field.");
            out();
            return true;
        }
        setField(field, container, parseField(field));
        out();
        return true;
    }

    protected abstract Object parseField(Field field);

    protected void checkId(int id) {
        Preconditions.checkState(id > 0, "Bad id:" + id);
    }

    @NonNull
    @Override
    public Component.Android scope() {
        return Component.Android.NoLimit;
    }
}
