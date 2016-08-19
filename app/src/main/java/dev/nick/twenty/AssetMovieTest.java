package dev.nick.twenty;

import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.ui.ImageSeat;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

public class AssetMovieTest extends BaseTest {

    final String urlAssets = "assets://test.gif";

    @FindView(id = R.id.image)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_layout);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageLoader.shared()
                .loadMovie()
                .from(urlAssets)
                .errorListener(new ErrorListener() {
                    @Override
                    public void onError(@NonNull Cause cause) {
                        LoggerManager.getLogger(getClass()).error(cause);
                    }
                })
                .progressListener(new ProgressListener<Movie>() {
                    @Override
                    public void onStartLoading() {
                        LoggerManager.getLogger(getClass()).debug("onStartLoading");
                    }

                    @Override
                    public void onProgressUpdate(float progress) {
                        LoggerManager.getLogger(getClass()).debug("onProgressUpdate:" + progress);
                    }

                    @Override
                    public void onCancel() {
                        LoggerManager.getLogger(getClass()).debug("onCancel");
                    }

                    @Override
                    public void onComplete(Movie result) {
                        LoggerManager.getLogger(getClass()).debug("onComplete:" + result);
                    }
                })
                .into(new ImageSeat<Movie>() {
                    @Override
                    public void seat(@NonNull Movie image) {
                        LoggerManager.getLogger(getClass()).debug("seat");
                    }

                    @Override
                    public int getWidth() {
                        return 1000;
                    }

                    @Override
                    public int getHeight() {
                        return 500;
                    }

                    @Override
                    public void startAnimation(Animation animation) {
                        LoggerManager.getLogger(getClass()).debug("startAnimation");
                    }
                })
                .start();
    }
}
