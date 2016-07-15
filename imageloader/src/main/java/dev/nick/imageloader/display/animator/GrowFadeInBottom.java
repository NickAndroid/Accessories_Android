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

import dev.nick.imageloader.R;

public class GrowFadeInBottom extends ResAnimator {

    public GrowFadeInBottom(Context context) {
        super(context);
    }

    @Override
    int getAnimResId() {
        return R.anim.grow_fade_in_from_bottom;
    }
}
