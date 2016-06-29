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

package dev.nick.logger;

import java.util.HashMap;

public class LoggerManager {


    final static HashMap<String, Logger> sLoggers = new HashMap<>();


    public static Logger getLogger(Class propertyClz) {

        String propName = propertyClz.getSimpleName();

        synchronized (sLoggers) {
            if (sLoggers.containsKey(propName)) return sLoggers.get(propName);
            Logger logger = new LoggerImpl(new LogTagBuilder() {
                @Override
                public String buildLogTag(String prop) {
                    return prop;
                }
            }, new CallingInfoBuilderImpl(), propName);

            sLoggers.put(propName, logger);

            return logger;
        }
    }
}
