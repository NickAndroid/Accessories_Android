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

package dev.nick.imageloader.worker.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InterruptedIOException;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.ui.ImageQuality;
import dev.nick.imageloader.worker.DecodeSpec;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.ImageData;
import dev.nick.imageloader.worker.ImageFetcher;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class BitmapDisplayTask extends BaseDisplayTask<Bitmap> {

    private ImageData<Bitmap> mImageData;

    private DimenSpec mDimenSpec;
    private ImageQuality mQuality;

    private LoaderConfig mLoaderConfig;

    private ProgressListener<Bitmap> mProgressListener;
    private ErrorListener mErrorListener;

    private TaskInterrupter mDisplayTaskMonitor;

    private DisplayTaskRecord mTaskRecord;

    private Context mContext;

    private Bitmap mResult;

    public BitmapDisplayTask(Context context,
                             LoaderConfig loaderConfig,
                             TaskInterrupter displayTaskMonitor,
                             ImageData<Bitmap> url,
                             DimenSpec spec,
                             ImageQuality quality,
                             ProgressListener<Bitmap> progressListener,
                             ErrorListener errorListener,
                             DisplayTaskRecord taskRecord) {
        this.mContext = context;
        this.mLoaderConfig = loaderConfig;
        this.mDisplayTaskMonitor = displayTaskMonitor;
        this.mImageData = url;
        this.mDimenSpec = spec;
        this.mQuality = quality;
        this.mProgressListener = progressListener;
        this.mErrorListener = errorListener;
        this.mTaskRecord = taskRecord;
    }

    @Override
    public void run() {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (mDisplayTaskMonitor.interruptExecute(mTaskRecord)) return;

        ImageSource<Bitmap> source = mImageData.getSource();

        ImageFetcher<Bitmap> fetcher = source.getFetcher(mContext, mLoaderConfig);

        DecodeSpec decodeSpec = new DecodeSpec(mQuality, mDimenSpec);
        try {
            mResult = fetcher.fetchFromUrl(mImageData.getUrl(), decodeSpec, mProgressListener, mErrorListener);
        } catch (InterruptedIOException | InterruptedException ignored) {

        } catch (Exception e) {
            if (mErrorListener != null)
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
    public ImageData<Bitmap> getImageData() {
        return mImageData;
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