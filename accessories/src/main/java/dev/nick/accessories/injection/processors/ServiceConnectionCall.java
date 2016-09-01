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

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.reflect.Method;

import dev.nick.accessories.injection.utils.ReflectionUtils;

import static dev.nick.accessories.injection.utils.ReflectionUtils.makeAccessible;

class ServiceConnectionCall implements ServiceConnection {

    Method boundCall;
    Method unBoundCall;

    Object target;

    public ServiceConnectionCall(Method boundCall, Method unBoundCall, Object target) {
        this.boundCall = boundCall;
        this.unBoundCall = unBoundCall;
        this.target = target;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (boundCall != null) {
            makeAccessible(boundCall);
            ReflectionUtils.invokeMethod(boundCall, target);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (unBoundCall != null) {
            makeAccessible(unBoundCall);
            ReflectionUtils.invokeMethod(unBoundCall, target);
        }
    }
}
