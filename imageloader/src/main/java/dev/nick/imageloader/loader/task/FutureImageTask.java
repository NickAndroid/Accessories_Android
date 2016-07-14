package dev.nick.imageloader.loader.task;

import android.support.annotation.Nullable;

import java.util.concurrent.FutureTask;

import dev.nick.imageloader.BuildConfig;
import dev.nick.logger.LoggerManager;

public class FutureImageTask extends FutureTask<Void> {

    static final boolean DEBUG = BuildConfig.DEBUG;

    private DoneListener mListener;
    private ImageTask mImageTask;

    public FutureImageTask(ImageTask task, @Nullable DoneListener listener) {
        super(task);
        this.mImageTask = task;
        this.mListener = listener;
    }

    @Override
    protected void done() {
        super.done();
        if (DEBUG) LoggerManager.getLogger(getClass()).funcEnter();
        if (mListener != null) mListener.onDone(this);
    }

    public interface DoneListener {
        void onDone(FutureImageTask futureImageTask);
    }

    public ImageTask getImageTask() {
        return mImageTask;
    }
}
