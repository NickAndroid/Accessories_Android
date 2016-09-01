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

package dev.nick.accessories.injection.annotation.binding;

import android.content.Context;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.nick.accessories.common.Null;
import dev.nick.accessories.injection.annotation.common.Component;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Component({Component.Android.NoLimit})
@Documented
public @interface BindService {

    int DEFAULT_REQUEST_CODE = 0x10;

    String action() default "";

    String pkgName() default "";

    Class clazz() default Null.class;

    // Optional for all.
    CallMethod boundCallback() default @CallMethod("");

    // Optional for all.
    CallMethod unBoundCallback() default @CallMethod("");

    // Optional for Activity/Fragment.
    ServiceConnectionStub connectionStub() default @ServiceConnectionStub("");

    // Optional for all.
    int requestCode() default DEFAULT_REQUEST_CODE;

    // Optional for all.
    int flag() default Context.BIND_AUTO_CREATE;
}
