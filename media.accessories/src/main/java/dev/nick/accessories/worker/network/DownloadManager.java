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

package dev.nick.accessories.worker.network;

import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import dev.nick.accessories.Terminable;
import dev.nick.accessories.worker.ProgressListener;
import dev.nick.accessories.worker.result.ErrorListener;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

public interface DownloadManager extends Terminable {

    @NonNull
    Transaction beginTransaction();

    String endTransaction(Transaction transaction);

    String getDownloadDirPath();

    @Builder
    @Getter
    @Setter
    class Transaction {

        private String url;
        private ProgressListener progressListener;
        private ErrorListener errorListener;

        private int usingNetworkType = ConnectivityManager.TYPE_WIFI;
    }
}
