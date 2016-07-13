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

package dev.nick.imageloader.loader;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.UUID;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.loader.network.HttpImageDownloader;
import dev.nick.imageloader.loader.network.ImageDownloader;
import dev.nick.imageloader.loader.network.NetworkUtils;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.logger.LoggerManager;

public class NetworkImageFetcher extends BaseImageFetcher {

    private ImageFetcher mFileImageFetcher;

    private String mTmpDir;

    public NetworkImageFetcher(PathSplitter<String> splitter, ImageFetcher fileImageFetcher) {
        super(splitter);
        mFileImageFetcher = fileImageFetcher;
    }

    private void ensurePolicy() {
        CachePolicy policy = loaderConfig.getCachePolicy();
        boolean preferredExternal = policy.getPreferredLocation() == CachePolicy.Location.EXTERNAL;
        mTmpDir = context.getCacheDir().getPath();
        if (preferredExternal && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalCache = context.getExternalCacheDir();
            if (externalCache != null) {
                mTmpDir = externalCache.getPath();
            }
        }
        if (debug) LoggerManager.getLogger(getClass()).info("Using tmp fir:" + mTmpDir);
    }

    private String buildTmpFilePath() {
        return mTmpDir + File.separator + UUID.randomUUID();
    }

    @Override
    public ImageFetcher prepare(Context context, LoaderConfig config) {
        super.prepare(context, config);
        ensurePolicy();
        return this;
    }

    @Override
    public void fetchFromUrl(@NonNull String url,
                             @NonNull DecodeSpec decodeSpec,
                             @Nullable ProgressListener<BitmapResult> progressListener,
                             @Nullable ErrorListener errorListener)
            throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        boolean wifiOnly = loaderConfig.getNetworkPolicy().isOnlyOnWifi();
        boolean isOnLine = NetworkUtils.isOnline(context, wifiOnly);

        // No connection.
        if (!isOnLine) {
            callOnError(errorListener, new Cause(new IllegalStateException("No network is available.")));
            return;
        }

        LoggerManager.getLogger(getClass()).info("callOnStart:" + progressListener);
        callOnStart(progressListener);

        String tmpPath = buildTmpFilePath();

        if (debug) {
            LoggerManager.getLogger(getClass()).debug("Using tmp path for image download:" + tmpPath);
        }

        ImageDownloader<Boolean> downloader = new HttpImageDownloader(tmpPath);

        boolean ok = downloader.download(url, progressListener, errorListener);

        if (ok) {
            mFileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + tmpPath, decodeSpec, progressListener, errorListener);
        } else {
            callOnError(errorListener, new Cause(new UnknownError()));
        }

        // Delete the tmp file.
        if (!new File(tmpPath).delete()) {
            LoggerManager.getLogger(getClass()).warn("Failed to delete the tmp file:" + tmpPath);
        }
    }
}
