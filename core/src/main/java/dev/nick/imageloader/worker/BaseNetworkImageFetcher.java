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

package dev.nick.imageloader.worker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.worker.network.DownloadManager;
import dev.nick.imageloader.worker.network.DownloadManagerImpl;
import dev.nick.imageloader.worker.result.ErrorListener;

public class BaseNetworkImageFetcher<T> extends BaseImageFetcher<T> {

    private ImageFetcher<T> mFileImageFetcher;

    private DownloadManager mDownloadManager;

    public BaseNetworkImageFetcher(PathSplitter<String> splitter, ImageFetcher<T> fileImageFetcher) {
        super(splitter);
        mFileImageFetcher = fileImageFetcher;
    }

    @Override
    public ImageFetcher<T> prepare(Context context, LoaderConfig config) {
        super.prepare(context, config);
        mFileImageFetcher.prepare(context, config);
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

        DownloadManager.Transaction<String> transaction = mDownloadManager.beginTransaction().url(url)
                .errorListener(errorListener).progressListener(progressListener);

        callOnStart(progressListener);

        String receivedPath = mDownloadManager.endTransaction(transaction);

        boolean ok = receivedPath != null;

        if (ok)
            return mFileImageFetcher.fetchFromUrl(ImageSource.Prefix.FILE + receivedPath, decodeSpec,
                    progressListener, errorListener);

        return null;
    }
}
