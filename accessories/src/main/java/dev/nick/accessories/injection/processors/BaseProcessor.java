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

import dev.nick.accessories.logger.Logger;
import dev.nick.accessories.logger.LoggerManager;
import lombok.Getter;

abstract class BaseProcessor<C, T> implements Processor<C, T> {

    private Logger mLogger;
    @Getter
    private Context context;

    public BaseProcessor(Context appContext) {
        this.context = appContext;
        this.mLogger = LoggerManager.getLogger(getClass());
    }

    protected Object unSupported() {
        throw new UnsupportedOperationException();
    }

    protected void sdkTooLow(Object o) {
        report("Sdk to low for:" + o);
    }

    protected void report(Object o) {
        mLogger.info(o);
    }

    protected void in() {
        mLogger.funcEnter();
    }

    protected void out() {
        mLogger.funcExit();
    }
}
