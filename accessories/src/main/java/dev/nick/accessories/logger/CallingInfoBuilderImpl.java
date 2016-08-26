/*
 *  Copyright (c) 2015-2016 Nick Guo
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.nick.accessories.logger;

class CallingInfoBuilderImpl implements CallingInfoBuilder {

    @Override
    public String getCallingInfo() {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        if (traceElements.length <= 4) return null;
        StackTraceElement element = traceElements[4];
        return element.getMethodName()
                + "()@" +
                element.getFileName() +
                "#" +
                element.getLineNumber();
    }
}