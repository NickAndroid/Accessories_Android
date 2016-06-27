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

package dev.nick.imageloader.cache.disk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.nick.imageloader.ZImageLoader;
import dev.nick.imageloader.cache.Cache;

public class DiskCache implements Cache<String, Bitmap> {

    String mExternalCacheDir;
    String mCacheDir;

    boolean preferToExternal;

    boolean debug;

    final List<FileOperator> mRunningOps;

    public DiskCache(ZImageLoader.Config config, Context context) {
        preferToExternal = config.isPreferExternalStorageCache();
        debug = config.isDebug();
        mCacheDir = context.getCacheDir().getPath();
        if (preferToExternal) {
            File externalCache = context.getExternalCacheDir();
            if (externalCache != null)
                mExternalCacheDir = externalCache.getPath();
        }
        mRunningOps = new ArrayList<>();
    }

    @Override
    @WorkerThread
    @RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void cache(@NonNull String key, Bitmap value) {

        if (debug) {
            Log.d("ZImageLoader.DiskCache", "Trying to cache:" + key);
        }

        if (preferToExternal && mExternalCacheDir != null
                && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!new FileWriter(mExternalCacheDir, value, key).write())
                new FileWriter(mCacheDir, value, key).write();
        } else {
            if (mCacheDir == null) {
                // No cache area available.
                return;
            }
            new FileWriter(mCacheDir, value, key).write();
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    @WorkerThread
    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public Bitmap get(@NonNull String key) {

        Bitmap result;

        if (preferToExternal) {
            result = new FileReader(mExternalCacheDir, key).read();
            if (result != null) return result;
        }
        // Try to find from internal cache.
        return new FileReader(mCacheDir, key).read();
    }

    abstract class FileOperator {
        String getFileNameByKey(String key) {
            return String.valueOf(key.hashCode());
        }
    }

    class FileReader extends FileOperator {

        String dir;
        String key;

        String fileName;

        public FileReader(String dir, String key) {
            this.dir = dir;
            this.key = key;
            this.fileName = getFileNameByKey(key);
        }

        Bitmap read() {

            File in = new File(dir + File.separator + fileName);

            if (!in.exists()) {
                if (debug)
                    Log.e("ZImageLoader.DiskCache", "Cache file do not exists:" + in.getAbsolutePath());
                return null;
            }

            AtomicFileCompat atomicFile = new AtomicFileCompat(in);

            try {
                FileInputStream fis = atomicFile.openRead();
                Bitmap out = BitmapFactory.decodeStream(fis);
                if (debug) {
                    Log.d("ZImageLoader.DiskCache", "Success read file cache:" + in.getAbsolutePath());
                }
                fis.close();
                return out;
            } catch (FileNotFoundException e) {
                if (debug)
                    Log.e("ZImageLoader.DiskCache", "Cache file do not exists:" + Log.getStackTraceString(e));
                return null;
            } catch (IOException e) {
                Log.e("ZImageLoader.DiskCache", "Failed to close fis:" + Log.getStackTraceString(e));
                return null;
            }
        }
    }

    class FileWriter extends FileOperator {

        Bitmap in;
        String dir;
        String key;

        String fileName;

        public FileWriter(String dir, Bitmap in, String key) {
            this.dir = dir;
            this.in = in;
            this.key = key;
            this.fileName = getFileNameByKey(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileWriter that = (FileWriter) o;

            return key.equals(that.key);

        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        public boolean write() {

            if (hasOp(this)) {
                if (debug) {
                    Log.d("", "Ignore dup task for:" + fileName);
                }
                return true;
            }

            addOp(this);

            File out = new File(dir + File.separator + fileName);

            if (out.exists()) {
                if (debug)
                    Log.e("ZImageLoader.DiskCache", "Skip cache exists file:" + out.getAbsolutePath());
                removeOp(this);
                return true;
            }

            if (!out.getParentFile().exists() && !out.getParentFile().mkdirs()) {
                // Something went wrong, nothing to do.
                if (debug)
                    Log.e("ZImageLoader.DiskCache", "Failed to create dirs:" + out.getParentFile().getAbsolutePath());
                removeOp(this);
                return false;
            }
            try {
                if (!out.createNewFile()) {
                    // Something went wrong, nothing to do.
                    if (debug)
                        Log.e("ZImageLoader.DiskCache", "Failed to create file:" + out.getAbsolutePath());
                    removeOp(this);
                    return false;
                }
                AtomicFileCompat atomicFile = new AtomicFileCompat(out);
                FileOutputStream fos = atomicFile.startWrite();
                if (!in.compress(Bitmap.CompressFormat.PNG, 100 /*full*/, fos)) {
                    if (debug)
                        Log.e("ZImageLoader.DiskCache", "Failed to compress bitmap to file:" + out.getAbsolutePath());
                    removeOp(this);
                    atomicFile.failWrite(fos);
                    return false;
                }
                atomicFile.finishWrite(fos);
            } catch (IOException e) {
                // Something went wrong, nothing to do.
                if (debug)
                    Log.e("ZImageLoader.DiskCache", "IOException when create file:" + Log.getStackTraceString(e));
                removeOp(this);
                return false;
            }

            if (debug) {
                Log.d("ZImageLoader.DiskCache", "Success write bitmap to:" + out.getAbsolutePath());
            }
            removeOp(this);
            return true;
        }
    }

    void addOp(FileOperator op) {
        synchronized (mRunningOps) {
            mRunningOps.add(op);
        }
    }

    void removeOp(FileOperator op) {
        synchronized (mRunningOps) {
            mRunningOps.remove(op);
        }
    }

    boolean hasOp(FileOperator op) {
        synchronized (mRunningOps) {
            return mRunningOps.contains(op);
        }
    }
}
