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

package dev.nick.twenty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.nick.scalpel.ScalpelAutoActivity;
import com.nick.scalpel.annotation.binding.FindView;

import dev.nick.imageloader.ImageLoader;

public class Usage extends ScalpelAutoActivity {

    @FindView(id = R.id.text)
    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage);

        String string =
                "getExternalStorageUsage" +
                        MiscUtils.formatedFileSize(ImageLoader.shared(this).getExternalStorageUsage())
                        + "\n getInternalStorageUsage"
                        + MiscUtils.formatedFileSize(ImageLoader.shared(this).getInternalStorageUsage())
                        + "\n getMobileTrafficUsage"
                        + MiscUtils.formatedFileSize(ImageLoader.shared(this).getMobileTrafficUsage())
                        + "\n getWifiTrafficUsage"
                        + MiscUtils.formatedFileSize(ImageLoader.shared(this).getWifiTrafficUsage());

        textView.setText(string);
    }
}
