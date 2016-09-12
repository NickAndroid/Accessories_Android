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

package dev.nick.accessoriestest.binding;

import android.support.annotation.NonNull;

import com.google.guava.base.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import dev.nick.accessories.injection.Injector;
import dev.nick.accessories.injection.processors.FieldProcessor;

public class CustomProcessorTest {

    @NameLess
    Me me;

    void register() {
        Injector.shared().addFieldProcessor(new FieldProcessor(null) {
            @Override
            protected Object parseField(Field field) {
                return new Me();
            }

            @NonNull
            @Override
            public Class<? extends Annotation> targetAnnotation() {
                return NameLess.class;
            }

            @Override
            protected Class<?> expectedType() {
                return Me.class;
            }
        }, NameLess.class);
    }

    void test() {
        Injector.shared().inject(this);
        Preconditions.checkNotNull(me);
    }

    void start() {
        register();
        test();
    }

    @interface NameLess {
    }

    class Me {
    }
}
