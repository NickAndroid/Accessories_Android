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

package dev.nick.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InterruptedIOException;

import dev.nick.imageloader.ui.ImageQuality;
import dev.nick.imageloader.loader.DecodeSpec;
import dev.nick.imageloader.loader.ImageFetcher;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ImageSourceType;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.imageloader.loader.task.DisplayTask;
import dev.nick.imageloader.loader.task.DisplayTaskMonitor;
import dev.nick.imageloader.loader.task.DisplayTaskRecord;

public class BitmapDisplayTask implements DisplayTask<Bitmap> {

    private ImageSource<Bitmap> mImageSource;

    private ViewSpec mViewSpec;
    private ImageQuality mQuality;

    private LoaderConfig mLoaderConfig;

    private ProgressListener<Bitmap> mProgressListener;
    private ErrorListener mErrorListener;

    private DisplayTaskMonitor<Bitmap> mDisplayTaskMonitor;

    private DisplayTaskRecord mTaskRecord;

    private Context mContext;

    private Bitmap mResult;

    public BitmapDisplayTask(Context context,
                             LoaderConfig loaderConfig,
                             DisplayTaskMonitor<Bitmap> displayTaskMonitor,
                             ImageSource<Bitmap> url,
                             ViewSpec spec,
                             ImageQuality quality,
                             ProgressListener<Bitmap> progressListener,
                             ErrorListener errorListener,
                             DisplayTaskRecord taskRecord) {
        this.mContext = context;
        this.mLoaderConfig = loaderConfig;
        this.mDisplayTaskMonitor = displayTaskMonitor;
        this.mImageSource = url;
        this.mViewSpec = spec;
        this.mQuality = quality;
        this.mProgressListener = progressListener;
        this.mErrorListener = errorListener;
        this.mTaskRecord = taskRecord;
    }

    @Override
    public void run() {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (!mDisplayTaskMonitor.shouldRun(this)) return;

        ImageSourceType<Bitmap> source = mImageSource.getType();

        ImageFetcher<Bitmap> fetcher = source.getFetcher(mContext, mLoaderConfig);

        DecodeSpec decodeSpec = new DecodeSpec(mQuality, mViewSpec);
        try {
            mResult = fetcher.fetchFromUrl(mImageSource.getUrl(), decodeSpec, mProgressListener, mErrorListener);
        } catch (InterruptedIOException | InterruptedException ignored) {

        } catch (Exception e) {
            mErrorListener.onError(new Cause(e));
        }
    }

    @Override
    public DisplayTaskRecord getTaskRecord() {
        return mTaskRecord;
    }

    @Override
    public Bitmap call() throws Exception {
        run();
        return mResult;
    }

    @NonNull
    @Override
    public String getUrl() {
        return mImageSource.getUrl();
    }

    @Override
    public ProgressListener<Bitmap> getProgressListener() {
        return mProgressListener;
    }

    @Override
    public void setProgressListener(@Nullable ProgressListener<Bitmap> listener) {
        mProgressListener = listener;
    }
}