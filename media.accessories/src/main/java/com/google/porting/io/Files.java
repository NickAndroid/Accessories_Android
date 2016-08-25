/*
 * Copyright (C) 2007 The Guava Authors
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
package com.google.porting.io;

import com.google.porting.base.Preconditions;

import java.io.File;
import java.io.IOException;

import static com.google.porting.base.Preconditions.checkArgument;
import static com.google.porting.base.Preconditions.checkNotNull;


public class Files {

    /**
     * Maximum loop count when creating temp directories.
     */
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    /**
     * Atomically creates a new directory somewhere beneath the system's
     * temporary directory (as defined by the {@code java.io.tmpdir} system
     * property), and returns its name.
     * <p/>
     * <p>Use this method instead of {@link File#createTempFile(String, String)}
     * when you wish to create a directory, not a regular file.  A common pitfall
     * is to call {@code createTempFile}, delete the file and create a
     * directory in its place, but this leads a race condition which can be
     * exploited to create security vulnerabilities, especially when executable
     * files are to be written into the directory.
     * <p/>
     * <p>This method assumes that the temporary volume is writable, has free
     * inodes and free blocks, and that it will not be called thousands of times
     * per second.
     *
     * @return the newly-created directory
     * @throws IllegalStateException if the directory could not be created
     */
    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + TEMP_DIR_ATTEMPTS + " attempts (tried "
                + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }

    public static long size(File file) {
        if (!file.exists()) return -1;
        return file.length();
    }

    /**
     * Moves a file from one path to another. This method can rename a file
     * and/or move it to a different directory. In either case {@code to} must
     * be the target path for the file itself; not just the new name for the
     * file or the path to the new parent directory.
     *
     * @param from the source file
     * @param to   the destination file
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if {@code from.equals(to)}
     */
    public static void move(File from, File to) throws IOException {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkArgument(!from.equals(to),
                "Source %s and destination %s must be different", from, to);

        if (!from.renameTo(to)) {
            throw new IOException("Unable to delete " + from);
        }
    }
}
