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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import dev.nick.accessories.injection.annotation.binding.BindBitmap;
import dev.nick.accessories.media.loader.MediaLoader;

class BitmapProcessor extends FieldProcessor {

    public BitmapProcessor(Context appContext) {
        super(appContext);
    }

    @Override
    protected Object parseField(Field field) {
        BindBitmap bindBitmap = field.getAnnotation(BindBitmap.class);
        String url = bindBitmap.url();
        if (!TextUtils.isEmpty(url)) {
            MediaLoader accessory = MediaLoader.shared();
            return accessory.loadBitmap().from(url).startSynchronously();
        }
        int id = checkId(bindBitmap.value());
        return BitmapFactory.decodeResource(getContext().getResources(), id);
    }

    @NonNull
    @Override
    public Class<? extends Annotation> targetAnnotation() {
        return BindBitmap.class;
    }

    @Override
    protected Class<?> expectedType() {
        return Bitmap.class;
    }
}
