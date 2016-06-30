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

package dev.nick.imageloader.loader.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.util.Log;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.loader.ImageInfo;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class LoadingTask implements Runnable {

    String url;
    ImageInfo info;
    TaskCallback<Bitmap> callback;

    Context mContext;

    int id;
    int settableId;
    long upTime = System.currentTimeMillis();

    private Logger mLogger;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoadingTask loadingTask = (LoadingTask) o;

        if (id != loadingTask.id) return false;
        if (!url.equals(loadingTask.url)) return false;
        return info != null ? info.equals(loadingTask.info) : loadingTask.info == null;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (info != null ? info.hashCode() : 0);
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        return "LoadingTask{" +
                "id=" + id +
                ", upTime=" + upTime +
                '}';
    }

    public LoadingTask(Context context, TaskCallback<Bitmap> callback, int id, int settableId, ImageInfo info, String url) {
        this.callback = callback;
        this.id = id;
        this.settableId = settableId;
        this.info = info;
        this.url = url;
        this.mContext = context;
        this.mLogger = LoggerManager.getLogger(ImageLoader.class);
    }

    @Override
    public void run() {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (!callback.onPreStart(this)) return;

        mLogger.info("Running task:" + getTaskId() + ", for settle:" + getSettableId());

        ImageSource source = ImageSource.of(url);

        Bitmap bitmap;
        try {
            bitmap = source.getFetcher(mContext).fetchFromUrl(url, info);
            callback.onComplete(bitmap, this);
        } catch (Exception e) {
            callback.onError("Error when fetch image:" + Log.getStackTraceString(e));
        }
    }

    public int getTaskId() {
        return id;
    }

    public int getSettableId() {
        return settableId;
    }

    public interface TaskCallback<T> {
        boolean onPreStart(LoadingTask task);

        void onComplete(T result, LoadingTask task);

        void onError(String errMsg);
    }
}