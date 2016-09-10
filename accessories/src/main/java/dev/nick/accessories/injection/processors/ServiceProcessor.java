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
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.guava.base.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dev.nick.accessories.common.Null;
import dev.nick.accessories.injection.annotation.binding.BindService;
import dev.nick.accessories.injection.annotation.binding.CallMethod;
import dev.nick.accessories.injection.annotation.binding.ServiceConnectionStub;
import dev.nick.accessories.injection.utils.ReflectionUtils;
import lombok.SneakyThrows;

import static dev.nick.accessories.injection.utils.ReflectionUtils.findMethod;
import static dev.nick.accessories.injection.utils.ReflectionUtils.getField;
import static dev.nick.accessories.injection.utils.ReflectionUtils.invokeMethod;
import static dev.nick.accessories.injection.utils.ReflectionUtils.isBaseDataType;
import static dev.nick.accessories.injection.utils.ReflectionUtils.makeAccessible;
import static dev.nick.accessories.injection.utils.ReflectionUtils.setField;

public class ServiceProcessor extends FieldProcessor {

    public ServiceProcessor(Context appContext) {
        super(appContext);
    }

    @Override
    public boolean process(@NonNull final Object container, @NonNull final Field field) {
        in();

        makeAccessible(field);
        checkType(field);

        Object fieldObject = getField(field, container);
        if (fieldObject != null && !isBaseDataType(field.getType())) {
            report("Ignored for none null field.");
            out();
            return true;
        }

        // FIXME: 21/03/16 Ensure it is an AIDL service.
        boolean isIInterface = field.getType().isInterface();
        Preconditions.checkState(isIInterface, "Field:" + field.getName() + " is not an AIDL interface, is:" + field.getType());

        final BindService bindService = field.getAnnotation(BindService.class);
        int flag = bindService.flag();
        Intent intent = null;
        Class<?> clz = bindService.clazz();
        if (clz != Null.class) {
            intent = new Intent(getContext(), clz);

        } else {
            String action = bindService.action();
            Preconditions.checkState(!TextUtils.isEmpty(action), "Empty action!");
            String pkgName = bindService.pkgName();
            pkgName = (TextUtils.isEmpty(pkgName) ? getContext().getPackageName() : pkgName);
            intent = new Intent(action);
            intent.setPackage(pkgName);
        }

        report(intent);

        CallMethod callMethodOnBound = bindService.bindCallback();
        String callMethodOnBoundMethodName = callMethodOnBound.value();
        CallMethod callMethod = bindService.unBindCallback();
        String callMethodOnUnBoundMethodName = callMethod.value();

        Method boundCall = null;
        Method unBoundCall = null;

        if (!TextUtils.isEmpty(callMethodOnBoundMethodName)) {
            boundCall = findMethod(container.getClass(), callMethodOnBoundMethodName);
            Preconditions.checkNotNull(boundCall, "Unable to find method without params:" + callMethodOnBoundMethodName);
        }

        if (!TextUtils.isEmpty(callMethodOnUnBoundMethodName)) {
            unBoundCall = findMethod(container.getClass(), callMethodOnUnBoundMethodName);
            Preconditions.checkNotNull(unBoundCall, "Unable to find method without params:" + callMethodOnUnBoundMethodName);
        }

        report(boundCall);
        report(unBoundCall);

        ServiceConnection serviceConnection = new ServiceConnectionCall(boundCall, unBoundCall, container) {
            @Override
            @SneakyThrows
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Class serviceClass = field.getType();
                String stubClassName = serviceClass.getName() + "$Stub";
                Class stubClass = Class.forName(stubClassName);
                Method asInterface = findMethod(stubClass, "asInterface", IBinder.class);
                Object result = invokeMethod(asInterface, null, iBinder);
                setField(field, container, result);

                // Inject connection
                ServiceConnectionStub connectionStub = bindService.connectionStub();
                String stubName = connectionStub.value();
                if (!TextUtils.isEmpty(stubName)) {
                    Field stubField = ReflectionUtils.findField(container, stubName);
                    Preconditions.checkNotNull(stubField);
                    makeAccessible(stubField);
                    Object stub = getField(stubField, container);
                    Preconditions.checkState(stub == null, stubName + " Not null, can not be override.");
                    setField(stubField, container, this);
                    report("Injected stub: " + stubField);
                }
                super.onServiceConnected(componentName, iBinder);
            }

            @Override
            public String toString() {
                return "ServiceConnectionCall@" + hashCode();
            }
        };
        //noinspection WrongConstant
        getContext().bindService(intent, serviceConnection, flag);
        out();
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Annotation> targetAnnotation() {
        return BindService.class;
    }
}
