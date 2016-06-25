package dev.nick.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.display.DisplayOption;
import dev.nick.imageloader.display.FadeInImageAnimator;
import dev.nick.imageloader.display.ImageAnimator;
import dev.nick.imageloader.loader.ImageInfo;
import dev.nick.imageloader.loader.ImageSource;

public class ZImageLoader {

    private Context mContext;

    private Handler mUIThreadHandler;

    private CacheManager mCacheManager;

    private ImageAnimator mDefaultImageAnimator;

    private ExecutorService mExecutorService;

    private static ZImageLoader sLoader;

    private ZImageLoader(Context context) {
        this.mContext = context;
        mUIThreadHandler = new Handler(Looper.getMainLooper());
        mCacheManager = new CacheManager();
        mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public synchronized static void init(Context context) {
        if (sLoader == null)
            sLoader = new ZImageLoader(context);
    }

    public static ZImageLoader getInstance() {
        return sLoader;
    }

    public void displayImage(final String url, final ImageView view) {
        displayImage(url, view, getDefaultAnimator(), null);
    }

    public ImageAnimator getDefaultAnimator() {
        if (mDefaultImageAnimator == null) mDefaultImageAnimator = new FadeInImageAnimator();
        return mDefaultImageAnimator;
    }

    public void displayImage(final String url, ImageView view, DisplayOption option) {
        displayImage(url, view, null, option);
    }

    public void displayImage(final String url, ImageView view, ImageAnimator animator) {
        displayImage(url, view, animator, null);
    }

    public void displayImage(final String url,
                             final ImageView view,
                             final ImageAnimator animator,
                             final DisplayOption option) {

        final Bitmap cached = mCacheManager.get(url);

        if (cached != null) {
            applyImageSetting(cached, view, null);// Do not animate when loaded from cache.
            return;
        }

        final int imgResWhenLoading = option == null ? 0 : option.getImgResShowWhenLoading();
        final int imgResWhenError = option == null ? 0 : option.getImgResShowWhenError();

        mExecutorService.execute(new LoadTask(url, new ImageInfo(view.getWidth(), view.getHeight()),
                new TaskCallback<Bitmap>() {
            @Override
            public void onComplete(Bitmap result) {
                if (result == null) return;
                applyImageSetting(result, view, animator);
                mCacheManager.cache(url, result);
            }

            @Override
            public void onStart() {
                if (imgResWhenLoading > 0) {
                    applyImageSetting(imgResWhenLoading, view, null);
                }
            }

            @Override
            public void onError(String errMsg) {
                if (imgResWhenError > 0) {
                    applyImageSetting(imgResWhenError, view, null);
                }
                Log.e("ZImageLoader", errMsg);
            }
        }));
    }

    private void applyImageSetting(Bitmap bitmap, ImageView imageView, ImageAnimator animator) {
        imageView.setImageBitmap(bitmap);
        if (animator != null)
            animator.animate(imageView);
    }

    private void applyImageSetting(int resId, ImageView imageView, ImageAnimator animator) {
        imageView.setImageResource(resId);
        if (animator != null)
            animator.animate(imageView);
    }

    class LoadTask implements Runnable {

        String url;
        ImageInfo info;
        TaskCallback<Bitmap> callback;

        public LoadTask(String url, ImageInfo info, TaskCallback<Bitmap> callback) {
            this.url = url;
            this.info = info;
            this.callback = callback;
        }

        @Override
        public void run() {
            Runnable startRunnable = new Runnable() {
                @Override
                public void run() {
                    callback.onStart();
                }
            };
            mUIThreadHandler.post(startRunnable);
            ImageSource source = ImageSource.of(url);
            final Bitmap bitmap;
            try {
                bitmap = source.getFetcher(mContext).fetchFromUrl(url, info);
                if (bitmap == null) {
                    callOnError("Unknown error.");
                    return;
                }
                Runnable finishRunnable = new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(bitmap);
                    }
                };
                mUIThreadHandler.post(finishRunnable);
            } catch (Exception e) {
                e.printStackTrace();
                callOnError("Error when fetch image:" + Log.getStackTraceString(e));
            }
        }

        void callOnError(final String errMsg) {
            Runnable errorRunnable = new Runnable() {
                @Override
                public void run() {
                    callback.onError(errMsg);
                }
            };
            mUIThreadHandler.post(errorRunnable);
        }
    }

    interface TaskCallback<T> {
        void onStart();

        void onComplete(T result);

        void onError(String errMsg);
    }

}
