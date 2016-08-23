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

package dev.nick.imageloader.worker.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.cache.FileNameGenerator;
import dev.nick.imageloader.cache.KeyGenerator;
import dev.nick.imageloader.control.TrafficStats;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class DownloadManagerImpl implements DownloadManager {

    private static final String DOWNLOAD_DIR_NAME = "download";

    Context mContext;

    boolean mOnlyOnWifi;
    Logger mLogger;

    private TrafficStats mTrafficStats;

    private boolean mTrafficStatsEnabled;

    private FileNameGenerator mFileNameGenerator;
    private KeyGenerator mKeyGenerator;

    private String mDownloadDir;

    public DownloadManagerImpl(Context context, LoaderConfig config) {
        mLogger = LoggerManager.getLogger(getClass());
        mContext = context;

        NetworkPolicy networkPolicy = config.getNetworkPolicy();
        mOnlyOnWifi = networkPolicy.isOnlyOnWifi();
        mTrafficStatsEnabled = networkPolicy.isTrafficStatsEnabled();
        mTrafficStats = TrafficStats.from(mContext);

        CachePolicy cachePolicy = config.getCachePolicy();
        boolean preferredExternal = cachePolicy.getPreferredLocation() == CachePolicy.Location.EXTERNAL;

        mDownloadDir = mContext.getCacheDir().getParent() + File.separator + DOWNLOAD_DIR_NAME;

        if (preferredExternal && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalCache = mContext.getExternalCacheDir();
            if (externalCache != null) {
                mDownloadDir = externalCache.getPath() + File.separator + DOWNLOAD_DIR_NAME;
            }
        }

        mFileNameGenerator = cachePolicy.getFileNameGenerator();
        mKeyGenerator = cachePolicy.getKeyGenerator();

        mLogger.verbose("Using download dir:" + mDownloadDir);

        if (!new File(mDownloadDir).exists() && !new File(mDownloadDir).mkdirs()) {
            throw new IllegalStateException("Can not create folder for download.");
        }
    }

    private String buildDownloadFilePath(String url) {
        return mDownloadDir + File.separator + mFileNameGenerator.fromKey(mKeyGenerator.fromUrl(url));
    }

    @NonNull
    @Override
    public Transaction<String> beginTransaction() {
        mLogger.funcEnter();

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
            mLogger.verbose("No network is available, returning");
            throw new IllegalStateException("Network policy is not allowed.");
        }

        mLogger.verbose(String.format("Using %s for this transaction", decodeNetworkType(usingNetworkType)));

        return new Transaction<String>().usingNetworkType(usingNetworkType);
    }

    @Override
    public String endTransaction(final Transaction<String> transaction) {
        mLogger.funcEnter();
        ImageDownloader<String> downloader = new HttpImageDownloader(Files.createTempDir(), new HttpImageDownloader.ByteReadingListener() {
            @Override
            public void onBytesRead(byte[] bytes) {
                if (mTrafficStatsEnabled) {
                    switch (transaction.usingNetworkType) {
                        case ConnectivityManager.TYPE_MOBILE:
                            mTrafficStats.onMobileTrafficUsage(bytes.length);
                            break;
                        case ConnectivityManager.TYPE_WIFI: //fall
                        default:
                            mTrafficStats.onWifiTrafficUsage(bytes.length);
                            break;
                    }
                }
            }
        });
        String received = downloader.download(transaction.url, transaction.progressListener, transaction.errorListener);
        String result = buildDownloadFilePath(transaction.url);
        try {
            Files.move(new File(received), new File(result));
        } catch (IOException e) {
            mLogger.trace("Failed to move file:", e);
            return null;
        }
        return result;
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