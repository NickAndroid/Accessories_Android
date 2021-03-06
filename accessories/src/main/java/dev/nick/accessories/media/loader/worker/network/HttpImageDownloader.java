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

package dev.nick.accessories.media.loader.worker.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;

import dev.nick.accessories.media.loader.worker.ProgressListener;
import dev.nick.accessories.media.loader.worker.result.Cause;
import dev.nick.accessories.media.loader.worker.result.ErrorListener;
import dev.nick.accessories.logger.LoggerManager;

public class HttpImageDownloader implements ImageDownloader<String> {

    File mTmpDir;
    ByteReadingListener mByteReadingListener;

    public HttpImageDownloader(File tmpDir, ByteReadingListener listener) {
        this.mTmpDir = tmpDir;
        this.mByteReadingListener = listener;
    }

    @Override
    public String download(String url, ProgressListener progressListener, ErrorListener errorListener) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.connect();
            InputStream is = conn.getInputStream();
            float fileSize = (float) conn.getContentLength();
            if (fileSize < 1 || is == null) {
                if (errorListener != null) {
                    errorListener.onError(new Cause(new Error(String.format("Content for from %s length is 0.", url))));
                }
            } else {
                File tmpFile = new File(mTmpDir, String.valueOf(url.hashCode()));
                LoggerManager.getLogger(getClass()).verbose("Using tmp path:" + tmpFile.getPath());
                FileOutputStream fos = new FileOutputStream(tmpFile);
                byte[] bytes = new byte[1024];
                int len = -1;
                float downloadSize = 0f;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                    if (mByteReadingListener != null) {
                        mByteReadingListener.onBytesRead(bytes);
                    }
                    downloadSize += len;
                    float progress = downloadSize / fileSize;
                    if (progressListener != null) {
                        progressListener.onProgressUpdate(progress);
                    }
                }
                is.close();
                fos.close();
                return tmpFile.getPath();
            }
        } catch (InterruptedIOException ignored) {
        } catch (Exception e) {
            if (errorListener != null) {
                errorListener.onError(new Cause(e));
            }
        }
        return null;
    }

    @Override
    public long size(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.connect();
            return conn.getContentLength();
        } catch (Exception ignored) {

        }
        return -1;
    }

    public interface ByteReadingListener {
        void onBytesRead(byte[] bytes);
    }
}
