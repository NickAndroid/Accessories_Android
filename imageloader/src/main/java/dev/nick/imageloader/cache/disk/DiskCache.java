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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.nick.imageloader.cache.Cache;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.cache.FileNameGenerator;
import dev.nick.logger.LoggerManager;

public class DiskCache implements Cache<String, Bitmap> {

    private String mExternalCacheDir;
    private String mInternalCacheDir;

    private boolean mPreferToExternal;

    private Bitmap.CompressFormat mFormat;
    private int mQuality;

    private final List<FileOperator> mRunningOps;

    private FileNameGenerator mFileNameGenerator;

    public DiskCache(CachePolicy cachePolicy, Context context) {
        mPreferToExternal = cachePolicy.getPreferredLocation() == CachePolicy.Location.EXTERNAL;
        mInternalCacheDir = context.getCacheDir().getPath() + File.separator + cachePolicy.getCacheDirName();
        if (mPreferToExternal) {
            File externalCache = context.getExternalCacheDir();
            if (externalCache != null)
                mExternalCacheDir = externalCache.getPath() + File.separator + cachePolicy.getCacheDirName();
        }
        mRunningOps = new ArrayList<>();
        mFileNameGenerator = cachePolicy.getFileNameGenerator();
        mFormat = cachePolicy.getCompressFormat();
        mQuality = cachePolicy.getQuality();
    }

    @Override
    @WorkerThread
    public void cache(@NonNull String key, Bitmap value) {
        if (mPreferToExternal && mExternalCacheDir != null
                && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!new FileWriter(mExternalCacheDir, value, key).write())
                new FileWriter(mInternalCacheDir, value, key).write();
        } else {
            if (mInternalCacheDir == null) {
                // No cache area available.
                return;
            }
            new FileWriter(mInternalCacheDir, value, key).write();
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    @WorkerThread
    @Deprecated
    public Bitmap get(@NonNull String key) {

        Bitmap result;

        if (mPreferToExternal) {
            result = new FileReader(mExternalCacheDir, key).read();
            if (result != null) return result;
        }
        // Try to find from internal cache.
        return new FileReader(mInternalCacheDir, key).read();
    }

    public String getCachePath(@NonNull String key) {
        File in = new File(getFilePathByKey(key));

        if (!in.exists()) {
            LoggerManager.getLogger(getClass()).info("No disk file:" + in.getAbsolutePath());
            return null;
        }
        return in.getPath();
    }

    private String getFilePathByKey(String key) {
        String dir = mPreferToExternal ? mExternalCacheDir : mInternalCacheDir;
        return dir + File.separator + mFileNameGenerator.fromKey(key);
    }

    @Override
    public void evictAll() {
        deleteFilesByDirectory(new File(mExternalCacheDir));
        deleteFilesByDirectory(new File(mInternalCacheDir));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    abstract class FileOperator {
        String getFileNameByKey(String key) {
            return mFileNameGenerator.fromKey(key);
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
                LoggerManager.getLogger(getClass()).debug("Cache file do not exists:" + in.getAbsolutePath());
                return null;
            }

            AtomicFileCompat atomicFile = new AtomicFileCompat(in);

            try {
                FileInputStream fis = atomicFile.openRead();
                Bitmap out = BitmapFactory.decodeStream(fis);
                Log.d("DiskCache", "Success read file cache:" + in.getAbsolutePath());
                fis.close();
                return out;
            } catch (FileNotFoundException e) {
                LoggerManager.getLogger(getClass()).debug("Cache file do not exists:" + Log.getStackTraceString(e));
                return null;
            } catch (IOException e) {
                LoggerManager.getLogger(getClass()).debug("Failed to close fis:" + Log.getStackTraceString(e));
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
                Log.d("", "Ignore dup task for:" + fileName);
                return true;
            }

            addOp(this);

            File out = new File(dir + File.separator + fileName);

            if (out.exists()) {
                LoggerManager.getLogger(getClass()).debug("Skip cache exists file:" + out.getAbsolutePath());
                removeOp(this);
                return true;
            }

            if (!out.getParentFile().exists() && !out.getParentFile().mkdirs()) {
                // Something went wrong, nothing to do.
                LoggerManager.getLogger(getClass()).debug("Failed to create dirs:" + out.getParentFile().getAbsolutePath());
                removeOp(this);
                return false;
            }
            try {
                if (!out.createNewFile()) {
                    // Something went wrong, nothing to do.
                    LoggerManager.getLogger(getClass()).debug("Failed to create file:" + out.getAbsolutePath());
                    removeOp(this);
                    return false;
                }
                AtomicFileCompat atomicFile = new AtomicFileCompat(out);
                FileOutputStream fos = atomicFile.startWrite();
                if (!in.compress(DiskCache.this.mFormat, DiskCache.this.mQuality, fos)) {
                    LoggerManager.getLogger(getClass()).debug("Failed to compress bitmap to file:" + out.getAbsolutePath());
                    removeOp(this);
                    atomicFile.failWrite(fos);
                    return false;
                }
                atomicFile.finishWrite(fos);
            } catch (IOException e) {
                // Something went wrong, nothing to do.
                LoggerManager.getLogger(getClass()).debug("IOException when create file:" + Log.getStackTraceString(e));
                removeOp(this);
                return false;
            }
            Log.d("DiskCache", "Success write bitmap to:" + out.getAbsolutePath());
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
