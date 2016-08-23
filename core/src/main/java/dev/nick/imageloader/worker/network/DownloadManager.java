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

import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.ErrorListener;

public interface DownloadManager {

    @NonNull
    Transaction<String> beginTransaction();

    String endTransaction(Transaction<String> transaction);

    class Transaction<T> {

        String url;
        ProgressListener progressListener;
        ErrorListener errorListener;

        int usingNetworkType = ConnectivityManager.TYPE_WIFI;

        Transaction() {
        }

        public Transaction<T> url(String url) {
            this.url = url;
            return this;
        }

        public Transaction<T> progressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        public Transaction<T> errorListener(ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        Transaction<T> usingNetworkType(int usingNetworkType) {
            this.usingNetworkType = usingNetworkType;
            return this;
        }
    }
}
