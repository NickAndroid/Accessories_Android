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

package dev.nick.accessories.injection;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.google.guava.base.Preconditions;

import org.xmlpull.v1.XmlPullParser;

import dev.nick.accessories.R;
import dev.nick.accessories.logger.LoggerManager;
import lombok.Getter;

abstract class AbsProcessorXmlParser {

    private Context mContext;

    public AbsProcessorXmlParser(@NonNull Context context) {
        this.mContext = context;
    }

    public void parse(int xmlRes) {
        String nameSpace = mContext.getResources().getString(R.string.processor_ns);
        try {
            XmlResourceParser parser = mContext.getResources().getXml(xmlRes);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG &&
                        parser.getName().equals(nameSpace)) {
                    final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.processor);
                    String scope = a.getString(R.styleable.processor_scope);
                    String clz = a.getString(R.styleable.processor_clazz);
                    Preconditions.checkNotNull(scope);
                    Preconditions.checkNotNull(clz);
                    onCreateItem(new ProcessorItem(clz, scope));
                    a.recycle();
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            LoggerManager.getLogger(getClass()).error("Received exception parsing bean xml:" + Log.getStackTraceString(e));
        }
    }

    protected void onCreateItem(ProcessorItem item) {
        // Noop.
    }

    @Getter
    class ProcessorItem {

        private String clz;
        private String scope;

        public ProcessorItem(String clz, String scope) {
            this.clz = clz;
            this.scope = scope;
        }
    }
}
