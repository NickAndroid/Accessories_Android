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
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InterruptedIOException;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.display.ImageQuality;
import dev.nick.imageloader.loader.DecodeSpec;
import dev.nick.imageloader.loader.ImageFetcher;
import dev.nick.imageloader.loader.ImageSource;
import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.ViewSpec;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class ImageTaskImpl implements ImageTask {

    private String mUrl;

    private ViewSpec mViewSpec;
    private ImageQuality mQuality;

    private LoaderConfig mLoaderConfig;

    private ProgressListener<BitmapResult> mProgressListener;
    private ErrorListener mErrorListener;

    private TaskMonitor mTaskMonitor;

    private ImageTaskRecord mTaskRecord;

    private Context mContext;

    public ImageTaskImpl(Context context,
                         LoaderConfig loaderConfig,
                         TaskMonitor taskMonitor,
                         String url,
                         ViewSpec spec,
                         ImageQuality quality,
                         ProgressListener<BitmapResult> progressListener,
                         ErrorListener errorListener,
                         ImageTaskRecord taskRecord) {
        this.mContext = context;
        this.mLoaderConfig = loaderConfig;
        this.mTaskMonitor = taskMonitor;
        this.mUrl = url;
        this.mViewSpec = spec;
        this.mQuality = quality;
        this.mProgressListener = progressListener;
        this.mErrorListener = errorListener;
        this.mTaskRecord = taskRecord;
    }

    @Override
    public void run() {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (!mTaskMonitor.shouldRun(this)) return;

        ImageSource source = ImageSource.of(mUrl);

        ImageFetcher fetcher = source.getFetcher(mContext, mLoaderConfig);

        DecodeSpec decodeSpec = new DecodeSpec(mQuality, mViewSpec);
        try {
            fetcher.fetchFromUrl(mUrl, decodeSpec, mProgressListener, mErrorListener);
        } catch (InterruptedIOException | InterruptedException ignored) {

        } catch (Exception e) {
            mErrorListener.onError(new Cause(e));
        }
    }

    @Override
    public ImageTaskRecord getTaskRecord() {
        return mTaskRecord;
    }

    @Override
    public Void call() throws Exception {
        run();
        return null;
    }

    @NonNull
    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public ProgressListener<BitmapResult> getProgressListener() {
        return mProgressListener;
    }

    @Override
    public void setProgressListener(@Nullable ProgressListener<BitmapResult> listener) {
        mProgressListener = listener;
    }
}