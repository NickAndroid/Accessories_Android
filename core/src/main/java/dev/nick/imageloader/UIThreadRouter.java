package dev.nick.imageloader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.result.Cause;
import dev.nick.imageloader.worker.result.ErrorListener;

class UIThreadRouter implements Handler.Callback {

    private static final int MSG_CALL_ON_START = 0x2;
    private static final int MSG_CALL_PROGRESS_UPDATE = 0x3;
    private static final int MSG_CALL_ON_COMPLETE = 0x4;
    private static final int MSG_CALL_ON_FAILURE = 0x5;
    private static final int MSG_CALL_ON_CANCEL = 0x6;

    private Handler mUIThreadHandler;

    private UIThreadRouter() {
        this.mUIThreadHandler = new Handler(Looper.getMainLooper(), this);
    }

    private static UIThreadRouter sharedRouter;

    public synchronized static UIThreadRouter getSharedRouter() {
        if (sharedRouter == null) sharedRouter = new UIThreadRouter();
        return sharedRouter;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_CALL_ON_START:
                onCallOnStart((ProgressListener) message.obj);
                break;
            case MSG_CALL_ON_COMPLETE:
                onCallOnComplete((CompleteParams) message.obj);
                break;
            case MSG_CALL_ON_FAILURE:
                onCallOnFailure((FailureParams) message.obj);
                break;
            case MSG_CALL_ON_CANCEL:
                onCallOnCancel((ProgressListener) message.obj);
                break;
            case MSG_CALL_PROGRESS_UPDATE:
                onCallOnProgressUpdate((ProgressParams) message.obj);
                break;
        }
        return true;
    }

    <T> void callOnStart(ProgressListener<T> listener) {
        if (listener != null) {
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_START, listener).sendToTarget();
        }
    }

    <T> void callOnCancel(ProgressListener<T> listener) {
        if (listener != null) {
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_CANCEL, listener).sendToTarget();
        }
    }

    <T> void callOnProgressUpdate(ProgressListener<T> listener, float progress) {
        if (listener != null) {
            ProgressParams progressParams = new ProgressParams();
            progressParams.progress = progress;
            progressParams.progressListener = listener;
            mUIThreadHandler.obtainMessage(MSG_CALL_PROGRESS_UPDATE, progressParams).sendToTarget();
        }
    }

    <T> void callOnComplete(ProgressListener<T> listener, T result) {
        if (listener != null) {
            CompleteParams completeParams = new CompleteParams();
            completeParams.progressListener = listener;
            completeParams.result = result;
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_COMPLETE, completeParams).sendToTarget();
        }
    }

    void callOnFailure(ErrorListener listener, Cause cause) {
        if (listener != null) {
            FailureParams failureParams = new FailureParams();
            failureParams.cause = cause;
            failureParams.listener = listener;
            mUIThreadHandler.obtainMessage(MSG_CALL_ON_FAILURE, failureParams).sendToTarget();
        }
    }

    private void onCallOnStart(ProgressListener listener) {
        listener.onStartLoading();
    }

    private void onCallOnCancel(ProgressListener listener) {
        listener.onCancel();
    }

    private <T> void onCallOnProgressUpdate(ProgressParams<T> progressParams) {
        progressParams.progressListener.onProgressUpdate(progressParams.progress);
    }

    private <T> void onCallOnComplete(CompleteParams<T> params) {
        params.progressListener.onComplete(params.result);
    }

    private void onCallOnFailure(FailureParams params) {
        params.listener.onError(params.cause);
    }

    private static class FailureParams {
        Cause cause;
        ErrorListener listener;
    }

    private static class CompleteParams<T> {
        T result;
        ProgressListener<T> progressListener;
    }

    private static class ProgressParams<T> {
        float progress;
        ProgressListener<T> progressListener;
    }
}
