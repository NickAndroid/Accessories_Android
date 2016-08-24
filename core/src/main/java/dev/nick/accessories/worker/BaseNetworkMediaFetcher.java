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

package dev.nick.accessories.worker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.accessories.AccessoryConfig;
import dev.nick.accessories.worker.network.DownloadManager;
import dev.nick.accessories.worker.network.DownloadManagerImpl;
import dev.nick.accessories.worker.result.ErrorListener;

public class BaseNetworkMediaFetcher<T> extends BaseMediaFetcher<T> {

    private MediaFetcher<T> mFileMediaFetcher;

    private DownloadManager mDownloadManager;

    public BaseNetworkMediaFetcher(PathSplitter<String> splitter, MediaFetcher<T> fileMediaFetcher) {
        super(splitter);
        mFileMediaFetcher = fileMediaFetcher;
    }

    @Override
    public MediaFetcher<T> prepare(Context context, AccessoryConfig config) {
        super.prepare(context, config);
        mFileMediaFetcher.prepare(context, config);
        mDownloadManager = new DownloadManagerImpl(context, config);
        return this;
    }

    @Override
    public T fetchFromUrl(@NonNull String url,
                          @NonNull final DecodeSpec decodeSpec,
                          @Nullable ProgressListener<T> progressListener,
                          @Nullable ErrorListener errorListener)
            throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        mLogger.funcEnter();

        DownloadManager.Transaction transaction = mDownloadManager.beginTransaction();
        transaction.setUrl(url);
        transaction.setErrorListener(errorListener);
        transaction.setProgressListener(progressListener);

        callOnStart(progressListener);

        String receivedPath = mDownloadManager.endTransaction(transaction);

        boolean ok = receivedPath != null;

        if (ok)
            return mFileMediaFetcher.fetchFromUrl(MediaSource.Prefix.FILE + receivedPath, decodeSpec,
                    progressListener, errorListener);

        return null;
    }

    @Override
    public void terminate() {
        super.terminate();
        mDownloadManager.terminate();
    }
}
