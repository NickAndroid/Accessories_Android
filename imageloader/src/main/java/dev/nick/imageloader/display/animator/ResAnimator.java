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

package dev.nick.imageloader.display.animator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.animation.AnimationUtils;

import dev.nick.imageloader.display.ImageSettable;

public abstract class ResAnimator implements ImageAnimator {

    protected Context context;

    public ResAnimator(Context context) {
        this.context = context;
    }

    @Override
    public void animate(@NonNull ImageSettable settable) {
        settable.startAnimation(AnimationUtils.loadAnimation(context, getAnimResId()));
    }

    abstract int getAnimResId();

    @Override
    public long getDuration() {
        return context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }
}
