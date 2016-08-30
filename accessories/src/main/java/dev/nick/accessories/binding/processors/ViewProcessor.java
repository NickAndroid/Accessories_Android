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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.guava.base.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import dev.nick.accessories.binding.annotation.binding.BindView;

import static dev.nick.accessories.binding.utils.ReflectionUtils.getField;
import static dev.nick.accessories.binding.utils.ReflectionUtils.isBaseDataType;
import static dev.nick.accessories.binding.utils.ReflectionUtils.makeAccessible;
import static dev.nick.accessories.binding.utils.ReflectionUtils.setField;

class ViewProcessor extends FieldProcessor {

    public ViewProcessor(Context appContext) {
        super(appContext);
    }

    @Override
    public boolean process(@Nullable Object container, @NonNull Field field) {
        in();
        makeAccessible(field);
        Object fieldObject = getField(field, container);
        if (fieldObject != null && !isBaseDataType(field.getType())) {
            report("Ignored for none null field.");
            out();
            return true;
        }
        setField(field, container, parseField(container, field));
        out();
        return true;
    }

    @Override
    protected Object parseField(Field field) {
        unSupported();
        return null;
    }

    protected Object parseField(Object container, Field field) {
        BindView bindView = field.getAnnotation(BindView.class);
        int id = checkId(bindView.value());
        View rootView;
        boolean isProvider = container instanceof BindView.RootViewProvider;
        if (isProvider) {
            BindView.RootViewProvider provider = (BindView.RootViewProvider) container;
            rootView = provider.getRootView();
        } else if (container instanceof Activity) {
            Activity activity = (Activity) container;
            rootView = activity.getWindow().getDecorView();
        } else if (container instanceof Fragment) {
            Fragment fragment = (Fragment) container;
            rootView = fragment.getView();
        } else if (container instanceof android.support.v4.app.Fragment) {
            android.support.v4.app.Fragment fragmentV4 = (android.support.v4.app.Fragment) container;
            rootView = fragmentV4.getView();
        } else {
            throw new IllegalArgumentException("You should impl BindView.RootViewProvider for no-component class");
        }
        Preconditions.checkNotNull(rootView, "No root view defined.");
        return rootView.findViewById(id);
    }

    @NonNull
    @Override
    public Class<? extends Annotation> targetAnnotation() {
        return BindView.class;
    }
}
