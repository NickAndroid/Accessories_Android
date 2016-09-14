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

package dev.nick.accessories.media.loader.worker.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InterruptedIOException;

import dev.nick.accessories.media.loader.LoaderConfig;
import dev.nick.accessories.media.loader.ui.MediaQuality;
import dev.nick.accessories.media.loader.worker.DecodeSpec;
import dev.nick.accessories.media.loader.worker.DimenSpec;
import dev.nick.accessories.media.loader.worker.MediaData;
import dev.nick.accessories.media.loader.worker.MediaFetcher;
import dev.nick.accessories.media.loader.worker.MediaSource;
import dev.nick.accessories.media.loader.worker.ProgressListener;
import dev.nick.accessories.media.loader.worker.result.Cause;
import dev.nick.accessories.media.loader.worker.result.ErrorListener;
import dev.nick.accessories.logger.LoggerManager;

public class BitmapDisplayTask extends BaseDisplayTask<Bitmap> {

    private MediaData<Bitmap> mMediaData;

    private DimenSpec mDimenSpec;
    private MediaQuality mQuality;

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
                             MediaData<Bitmap> url,
                             DimenSpec spec,
                             MediaQuality quality,
                             ProgressListener<Bitmap> progressListener,
                             ErrorListener errorListener,
                             DisplayTaskRecord taskRecord) {
        this.mContext = context;
        this.mLoaderConfig = loaderConfig;
        this.mDisplayTaskMonitor = displayTaskMonitor;
        this.mMediaData = url;
        this.mDimenSpec = spec;
        this.mQuality = quality;
        this.mProgressListener = progressListener;
        this.mErrorListener = errorListener;
        this.mTaskRecord = taskRecord;
    }

    @Override
    public void run() {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (mDisplayTaskMonitor.interruptExecute(mTaskRecord)) {
            LoggerManager.getLogger(getClass()).verbose("interruptExecute!");
            return;
        }

        try {
            MediaSource<Bitmap> source = mMediaData.getSource();
            MediaFetcher<Bitmap> fetcher = source.getFetcher(mContext, mLoaderConfig);
            DecodeSpec decodeSpec = new DecodeSpec(mQuality, mDimenSpec);
            mResult = fetcher.fetchFromUrl(mMediaData.getUrl(), decodeSpec, mProgressListener, mErrorListener);
        } catch (InterruptedIOException | InterruptedException ignored) {
            LoggerManager.getLogger(getClass()).debug("Ignored error:" + ignored.getLocalizedMessage());
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
    public MediaData<Bitmap> getImageData() {
        return mMediaData;
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