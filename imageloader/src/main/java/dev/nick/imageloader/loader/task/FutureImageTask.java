package dev.nick.imageloader.loader.task;

import android.support.annotation.Nullable;

import java.util.concurrent.FutureTask;

import dev.nick.logger.LoggerManager;

public class FutureImageTask extends FutureTask<Void> {

    private DoneListener mListener;
    private ImageTask mImageTask;

    private boolean mCancelOthersBeforeRun;

    public FutureImageTask(ImageTask task, @Nullable DoneListener listener, boolean cancelOthersBeforeRun) {
        super(task);
        this.mImageTask = task;
        this.mListener = listener;
        this.mCancelOthersBeforeRun = cancelOthersBeforeRun;
    }

    @Override
    protected void done() {
        super.done();
        LoggerManager.getLogger(getClass()).funcEnter();
        if (mListener != null) mListener.onDone(this);
    }

    public interface DoneListener {
        void onDone(FutureImageTask futureImageTask);
    }

    public boolean shouldCancelOthersBeroreRun() {
        return mCancelOthersBeforeRun;
    }

    public ImageTask getImageTask() {
        return mImageTask;
    }
}
