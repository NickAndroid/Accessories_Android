package dev.nick.imageloader.loader.network;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;

import dev.nick.imageloader.loader.ProgressListener;
import dev.nick.imageloader.loader.result.Cause;
import dev.nick.imageloader.loader.result.ErrorListener;
import dev.nick.logger.LoggerManager;

public class HttpImageDownloader implements ImageDownloader<Boolean> {

    String mTmpFilePath;

    public HttpImageDownloader(String tmpFilePath) {
        this.mTmpFilePath = tmpFilePath;
    }

    @Override
    public Boolean download(String url, ProgressListener progressListener, ErrorListener errorListener) {
        LoggerManager.getLogger(getClass()).info("download:" + url);
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.connect();
            InputStream is = conn.getInputStream();
            float fileSize = (float) conn.getContentLength();
            if (fileSize < 1 || is == null) {
                if (errorListener != null) {
                    errorListener.onError(new Cause(new Error(String.format("Content for url %s length is 0.", url))));
                }
            } else {
                FileOutputStream fos = new FileOutputStream(mTmpFilePath);
                byte[] bytes = new byte[1024];
                int len = -1;
                float downloadSize = 0f;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                    downloadSize += len;
                    float progress = downloadSize / fileSize;
                    if (progressListener != null) {
                        progressListener.onProgressUpdate(progress);
                    }
                }
                is.close();
                fos.close();
                return Boolean.TRUE;
            }
        } catch (InterruptedIOException ignored) {
        } catch (Exception e) {
            if (errorListener != null) {
                errorListener.onError(new Cause(e));
            }
        }
        return Boolean.FALSE;
    }
}
