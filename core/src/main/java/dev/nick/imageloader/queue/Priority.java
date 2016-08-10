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

package dev.nick.imageloader.queue;

public enum Priority {

    HIGH(0, new Sequencer<Priority>() {
        @Override
        public Priority lower() {
            return NORMAL;
        }

        @Override
        public Priority higher() {
            return null;
        }

        @Override
        public boolean isHigherThan(Priority priority) {
            return true;
        }

        @Override
        public boolean isLowerThan(Priority priority) {
            return false;
        }
    }),
    NORMAL(0, new Sequencer<Priority>() {
        @Override
        public Priority lower() {
            return LOW;
        }

        @Override
        public Priority higher() {
            return HIGH;
        }

        @Override
        public boolean isHigherThan(Priority priority) {
            return priority == LOW;
        }

        @Override
        public boolean isLowerThan(Priority priority) {
            return priority == HIGH;
        }
    }),
    LOW(0, new Sequencer<Priority>() {
        @Override
        public Priority lower() {
            return null;
        }

        @Override
        public Priority higher() {
            return NORMAL;
        }

        @Override
        public boolean isHigherThan(Priority priority) {
            return false;
        }

        @Override
        public boolean isLowerThan(Priority priority) {
            return true;
        }
    });

    long timeoutMillSec;
    Sequencer<Priority> sequencer;

    Priority(long timeoutMillSec, Sequencer<Priority> sequencer) {
        this.timeoutMillSec = timeoutMillSec;
        this.sequencer = sequencer;
    }

    interface Sequencer<T> {
        T lower();

        T higher();

        boolean isHigherThan(T t);

        boolean isLowerThan(T t);
    }
}
