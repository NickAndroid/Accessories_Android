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
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.UUID;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.cache.FileNameGenerator;
import dev.nick.imageloader.cache.KeyGenerator;
import dev.nick.imageloader.control.TrafficStats;
import dev.nick.imageloader.worker.network.HttpImageDownloader;
import dev.nick.imageloader.worker.network.ImageDownloader;
import dev.nick.imageloader.worker.network.NetworkPolicy;
import dev.nick.imageloader.worker.network.NetworkUtils;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class NetworkImageFetcher extends BaseImageFetcher<Bitmap> {

    private static final String DOWNLOAD_DIR_NAME = "download";

    private ImageFetcher<Bitmap> mFileImageFetcher;

    private String mDownloadDir;

    private FileNameGenerator mFileNameGenerator;
    private KeyGenerator mKeyGenerator = new KeyGenerator() {
        @NonNull
        @Override
        public String fromUrl(@NonNull String url) {
            // Careless the spec info.
            return String.valueOf(url.hashCode());
        }
    };

    private boolean mOnlyOnWifi;
    private boolean mTrafficStatsEnabled;

    private TrafficStats mTrafficStats;

    public NetworkImageFetcher(PathSplitter<String> splitter, ImageFetcher<Bitmap> fileImageFetcher) {
        super(splitter);
        mFileImageFetcher = fileImageFetcher;
    }

    private void ensurePolicy() {
        CachePolicy cachePolicy = mLoaderConfig.getCachePolicy();
        boolean preferredExternal = cachePolicy.getPreferredLocation() == CachePolicy.Location.EXTERNAL;
        mDownloadDir = mContext.getCacheDir().getParent() + File.separator + DOWNLOAD_DIR_NAME;
        if (preferredExternal && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalCache = mContext.getExternalCacheDir();
            if (externalCache != null) {
                mDownloadDir = externalCache.getPath() + File.separator + DOWNLOAD_DIR_NAME;
            }
        }
        mFileNameGenerator = cachePolicy.getFileNameGenerator();
        mLogger.verbose("Using download dir:" + mDownloadDir);
        if (!new File(mDownloadDir).exists() && !new File(mDownloadDir).mkdirs()) {
            throw new IllegalStateException("Can not create folder for download.");
        }
        NetworkPolicy networkPolicy = mLoaderConfig.getNetworkPolicy();
        mOnlyOnWifi = networkPolicy.isOnlyOnWifi();
        mTrafficStatsEnabled = networkPolicy.isTrafficStatsEnabled();
        if (mTrafficStatsEnabled) {
            mTrafficStats = TrafficStats.from(mContext);
        }
    }

    private String buildTmpFilePath() {
        return mDownloadDir + File.separator + UUID.randomUUID();
    }

    private String buildDownloadFilePath(String url) {
        return mDownloadDir + File.separator + mFileNameGenerator.fromKey(mKeyGenerator.fromUrl(url));
    }

    @Override
    public ImageFetcher<Bitmap> prepare(Context context, LoaderConfig config) {
        super.prepare(context, config);
        mFileImageFetcher.prepare(context, config);
        ensurePolicy();
        return this;
    }

    @Override
    public Bitmap fetchFromUrl(@NonNull String url,
                               @NonNull final DecodeSpec decodeSpec,
                               @Nullable ProgressListener<Bitmap> progressListener,
                               @Nullable ErrorListener errorListener)
            throws Exception {

        super.fetchFromUrl(url, decodeSpec, progressListener, errorListener);

        int usingNetworkType = ConnectivityManager.TYPE_WIFI;

        boolean isWifiOnLine = NetworkUtils.isWifiOnline(mContext);
        boolean isMobileOnLine = NetworkUtils.isMobileOnline(mContext);

        boolean readyToLoad = false;

        if (isWifiOnLine) {
            readyToLoad = true;
            usingNetworkType = ConnectivityManager.TYPE_WIFI;
        }

        if (!isWifiOnLine && !mOnlyOnWifi && isMobileOnLine) {
            readyToLoad = true;
            usingNetworkType = ConnectivityManager.TYPE_MOBILE;
        }

        // No connection.
        if (!readyToLoad) {
            callOnError(errorListener, new Cause(new IllegalStateException("No network is available.")));
            return null;
        }

        mLogger.verbose("Using network tye to download:" + decodeNetworkType(usingNetworkType));

        callOnStart(progressListener);

        String tmpPath = buildTmpFilePath();

        mLogger.verbose("Using tmp path for image download:" + tmpPath);

        HttpImageDownloader.ByteReadingListener byteReadingListener = null;
        if (mTrafficStatsEnabled) {
            final int finalUsingNetworkType = usingNetworkType;
            byteReadingListener = new HttpImageDownloader.ByteReadingListener() {
                @Override
                public void onBytesRead(byte[] bytes) {
                    switch (finalUsingNetworkType) {
                        case ConnectivityManager.TYPE_MOBILE:
                            mTrafficStats.onMobileTrafficUsage(bytes.length);
                            break;
                        case ConnectivityManager.TYPE_WIFI: //fall
                        default:
                            mTrafficStats.onWifiTrafficUsage(bytes.length);
                            break;
                    }
                }
            };
        }
        ImageDownloader<Boolean> downloader = new HttpImageDownloader(tmpPath, byteReadingListener);

        String downloadPath = buildDownloadFilePath(url);
        File downloadFile = new File(downloadPath);

        boolean exists = downloadFile.exists();

        if (exists) {
            try {
                mLogger.info("Using exist file instead of download.");
                mFileImageFetcher.fetchFromUrl(ImageSourceType.FILE.getPrefix() + downloadPath,
                        decodeSpec, progressListener, errorListener);
                return null;
            } catch (Exception e) {
                mLogger.warn("Error when fetch exists file:" + downloadPath);
                //noinspection ResultOfMethodCallIgnored
                downloadFile.delete();
            }
        }

        boolean ok = downloader.download(url, progressListener, errorListener);

        // Rename the file as download.
        if (ok && !new File(tmpPath).renameTo(downloadFile)) {
            mLogger.warn(String.format("Failed to move the tmp file from %s to %s", tmpPath, downloadPath));
        }
        // Delete the tmp file.
        if (!ok && !new File(tmpPath).delete()) {
            mLogger.verbose("Failed to delete the tmp file:" + tmpPath);
        }

        if (ok) {
            return mFileImageFetcher.fetchFromUrl(ImageSourceType.FILE.getPrefix() + tmpPath, decodeSpec, progressListener, errorListener);
        }
        return null;
    }

    String decodeNetworkType(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
                return "TYPE_MOBILE";
            default:
                return "WIFI-ETC";
        }
    }
}
