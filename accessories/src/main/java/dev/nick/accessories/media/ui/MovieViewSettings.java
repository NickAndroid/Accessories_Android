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

package dev.nick.accessories.media.ui;

import android.graphics.Movie;
import android.support.annotation.NonNull;

import dev.nick.accessories.media.ui.animator.ViewAnimator;

public class MovieViewSettings extends ViewSettings<Movie> {

    Movie mMovie;

    public MovieViewSettings(ViewAnimator<Movie> animator, @NonNull MediaHolder<Movie> mediaHolder, Movie movie) {
        super(animator, mediaHolder);
        this.mMovie = movie;
    }

    @Override
    protected void apply() {
        if (mMovie != null) {
            mSeat.seat(mMovie);
            if (mAnimator != null) {
                mAnimator.animate(mSeat);
            }
        }
    }
}
