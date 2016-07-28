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
import dev.nick.imageloader.cache.FileNameGenerator;
import dev.nick.imageloader.cache.KeyGenerator;
import dev.nick.imageloader.loader.network.HttpImageDownloader;
import dev.nick.imageloader.loader.network.ImageDownloader;
import dev.nick.imageloader.loader.network.NetworkUtils;
import dev.nick.imageloader.loader.result.BitmapResult;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;

public class NetworkImageFetcher extends BaseImageFetcher {

    private static final String DOWNLOAD_DIR_NAME = "download";

    private ImageFetcher mFileImageFetcher;

    private String mDownloadDir;

    private FileNameGenerator mFileNameGenerator;
    private KeyGenerator mKeyGenerator = new KeyGenerator() {
        @NonNull
        @Override
        public String fromUrl(@NonNull String url, ViewSpec info) {
            // Careless the spec info.
            return String.valueOf(url.hashCode());
        }
    };

    public NetworkImageFetcher(PathSplitter<String> splitter, ImageFetcher fileImageFetcher) {
        super(splitter);
        mFileImageFetcher = fileImageFetcher;
    }

    private void ensurePolicy() {
        CachePolicy policy = mLoaderConfig.getCachePolicy();
        boolean preferredExternal = policy.getPreferredLocation() == CachePolicy.Location.EXTERNAL;
        mDownloadDir = mContext.getCacheDir().getParent() + File.separator + DOWNLOAD_DIR_NAME;
        if (preferredExternal && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalCache = mContext.getExternalCacheDir();
            if (externalCache != null) {
                mDownloadDir = externalCache.getPath() + File.separator + DOWNLOAD_DIR_NAME;
            }
        }
        mFileNameGenerator = policy.getFileNameGenerator();
        mLogger.verbose("Using download dir:" + mDownloadDir);
        if (!new File(mDownloadDir).exists() && !new File(mDownloadDir).mkdirs()) {
            throw new IllegalStateException("Can not create folder for download.");
        }
    }

    private String buildTmpFilePath() {
        return mDownloadDir + File.separator + UUID.randomUUID();
    }

    private String buildDownloadFilePath(String url) {
        return mDownloadDir + File.separator + mFileNameGenerator.fromKey(mKeyGenerator.fromUrl(url, null));
    }

    @Override
    public ImageFetcher prepare(Context context, LoaderConfig config) {
        super.prepare(context, config);
        mFileImageFetcher.prepare(context, config);
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

        boolean wifiOnly = mLoaderConfig.getNetworkPolicy().isOnlyOnWifi();
        boolean isOnLine = NetworkUtils.isOnline(mContext, wifiOnly);

        // No connection.
        if (!isOnLine) {
            callOnError(errorListener, new Cause(new IllegalStateException("No network is available.")));
            return;
        }

        callOnStart(progressListener);

        String tmpPath = buildTmpFilePath();

        mLogger.verbose("Using tmp path for image download:" + tmpPath);

        ImageDownloader<Boolean> downloader = new HttpImageDownloader(tmpPath);

        String downloadPath = buildDownloadFilePath(url);
        File downloadFile = new File(downloadPath);

        boolean exists = downloadFile.exists();

        if (exists) {
            try {
                mLogger.info("Using exist file instead of download.");
                mFileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + downloadPath,
                        decodeSpec, progressListener, errorListener);
                return;
            } catch (Exception e) {
                mLogger.warn("Error when fetch exists file:" + downloadPath);
                //noinspection ResultOfMethodCallIgnored
                downloadFile.delete();
            }
        }

        boolean ok = downloader.download(url, progressListener, errorListener);

        if (ok) {
            mFileImageFetcher.fetchFromUrl(ImageSource.FILE.prefix + tmpPath, decodeSpec, progressListener, errorListener);
        }
        // Rename the file as download.
        if (ok && !new File(tmpPath).renameTo(downloadFile)) {
            mLogger.warn(String.format("Failed to move the tmp file from %s to %s", tmpPath, downloadPath));
        }
        // Delete the tmp file.
        if (!ok && !new File(tmpPath).delete()) {
            mLogger.verbose("Failed to delete the tmp file:" + tmpPath);
        }
    }
}
