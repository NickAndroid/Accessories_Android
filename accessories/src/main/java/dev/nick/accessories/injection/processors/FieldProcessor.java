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
import android.support.annotation.NonNull;

import com.google.guava.base.Preconditions;

import java.lang.reflect.Field;

import dev.nick.accessories.BuildConfig;
import dev.nick.accessories.injection.annotation.common.Component;
import lombok.Getter;

import static dev.nick.accessories.injection.utils.ReflectionUtils.getField;
import static dev.nick.accessories.injection.utils.ReflectionUtils.isBaseDataType;
import static dev.nick.accessories.injection.utils.ReflectionUtils.makeAccessible;
import static dev.nick.accessories.injection.utils.ReflectionUtils.setField;

public abstract class FieldProcessor extends BaseProcessor<Object, Field> {

    @Getter
    private boolean strictModeEnabled;

    public FieldProcessor(Context appContext) {
        super(appContext);
        strictModeEnabled = BuildConfig.DEBUG;
    }

    @Override
    public boolean process(@NonNull Object container, @NonNull Field field) {
        in();
        makeAccessible(field);
        checkType(field);
        Object fieldObject = getField(field, container);
        if (fieldObject != null && !isBaseDataType(field.getType())) {
            report("Ignored for none null field.");
            out();
            return true;
        }
        setField(field, container, parseField(field));
        out();
        return true;
    }

    protected Object parseField(Field field) {
        return null;
    }

    protected Class<?> expectedType() {
        return null;
    }

    protected void checkType(Field field) {
        if (strictModeEnabled && expectedType() != null)
            Preconditions.checkState(field.getType() == expectedType(), "Expected type is:" + expectedType());
    }

    protected int checkId(int id) {
        Preconditions.checkState(id > 0, "Bad id:" + id);
        return id;
    }

    public boolean asyncModeAllowed() {
        return false;
    }

    @NonNull
    @Override
    public Component.Android scope() {
        return Component.Android.NoLimit;
    }
}
