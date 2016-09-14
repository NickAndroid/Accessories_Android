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

package dev.nick.accessories.media;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.util.ArrayList;

import lombok.Synchronized;

public class ScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {

    ArrayList<String> mPaths = new ArrayList<>();

    MediaScannerConnection mScannerConnection;

    boolean mConnected;

    Runnable mRunnable;

    public ScannerClient(Context context) {
        mScannerConnection = new MediaScannerConnection(context, this);
    }

    @Synchronized
    public void scanPath(String path, Runnable doOnComplete) {
        mRunnable = doOnComplete;
        if (mConnected) {
            mScannerConnection.scanFile(path, null);
        } else {
            mPaths.add(path);
            mScannerConnection.connect();
        }
    }

    @Synchronized
    public void scanPath(String path) {
        scanPath(path, null);
    }

    @Override
    @Synchronized
    public void onMediaScannerConnected() {
        mConnected = true;
        if (!mPaths.isEmpty()) {
            for (String path : mPaths) {
                mScannerConnection.scanFile(path, null);
            }
            mPaths.clear();
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        if (mRunnable != null) mRunnable.run();
    }
}