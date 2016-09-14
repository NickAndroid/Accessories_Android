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

package dev.nick.accessories.media.loader.utils;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import dev.nick.accessories.logger.LoggerManager;

public abstract class FileUtils {
    public static long getFileSize(@NonNull File f) {
        long s = 0;
        if (Preconditions.checkNotNull(f).exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                s = fis.available();
            } catch (IOException e) {
                LoggerManager.getLogger(FileUtils.class).trace("IOException occur:", e);
            }
        }
        return s;
    }
}
