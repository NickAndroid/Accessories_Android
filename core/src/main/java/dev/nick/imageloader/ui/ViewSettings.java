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

package dev.nick.imageloader.ui;

import android.support.annotation.NonNull;

import dev.nick.imageloader.ui.animator.ViewAnimator;

public abstract class ViewSettings<T> implements Runnable {

    protected ViewAnimator<T> mAnimator;
    @NonNull
    protected MediaHolder<T> mSeat;

    public ViewSettings(ViewAnimator<T> animator,
                        @NonNull MediaHolder<T> mediaHolder) {
        this.mAnimator = animator;
        this.mSeat = mediaHolder;
    }

    protected abstract void apply();

    @Override
    public final void run() {
        apply();
    }

    @Override
    public String toString() {
        return "ViewSettings{" +
                "mAnimator=" + mAnimator +
                ", mSeat=" + mSeat +
                '}';
    }
}
