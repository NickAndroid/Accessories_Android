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

package dev.nick.accessories.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import dev.nick.accessories.logger.Logger;
import dev.nick.accessories.logger.LoggerManager;


/**
 * ServiceProxy is a superclass for proxy objects which make a single call to a service. It handles
 * connecting to the service, running a task supplied by the subclass when the connection is ready,
 * and disconnecting from the service afterwards. ServiceProxy objects cannot be reused (trying to
 * do so generates an {@link IllegalStateException}).
 * <p>
 * Subclasses must override {@link #onConnected} to store the binder. Then, when the subclass wants
 * to make a service call, it should call {@link #setTask}, supplying the {@link ProxyTask} that
 * should run when the connection is ready. {@link ProxyTask#run} should implement the necessary
 * logic to make the call on the service.
 */

public abstract class ServiceProxy {
    public static final String EXTRA_FORCE_SHUTDOWN = "ServiceProxy.FORCE_SHUTDOWN";

    private static final boolean DEBUG_PROXY = false; // DO NOT CHECK THIS IN SET TO TRUE
    protected final String mTag;
    protected final Intent mIntent;
    private final Context mContext;
    private final ServiceConnection mConnection = new ProxyConnection();
    private ProxyTask mTask;
    private String mName = " unnamed";
    // Service call timeout (in seconds)
    private int mTimeout = 45;
    private long mStartTime;
    private boolean mTaskSet = false;
    private boolean mTaskCompleted = false;

    private Logger mLogger;

    public ServiceProxy(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
        mTag = getClass().getSimpleName();
        if (Debug.isDebuggerConnected()) {
            mTimeout <<= 2;
        }
        mLogger = LoggerManager.getLogger(getClass());
    }

    /**
     * This function is called after the proxy connects to the service but before it runs its task.
     * Subclasses must override this to store the binder correctly.
     *
     * @param binder The service IBinder.
     */
    public abstract void onConnected(IBinder binder);

    public int getTimeout() {
        return mTimeout;
    }

    public ServiceProxy setTimeout(int secs) {
        mTimeout = secs;
        return this;
    }

    protected boolean setTask(ProxyTask task, String name) throws IllegalStateException {
        if (mTaskSet) {
            throw new IllegalStateException("Cannot call setTask twice on the same ServiceProxy.");
        }
        mTaskSet = true;
        mName = name;
        mTask = task;
        mStartTime = System.currentTimeMillis();
        if (DEBUG_PROXY) {
            mLogger.debug("Bind requested for task " + mName);
        }
        return mContext.bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Callers that want to wait on the {@link ProxyTask} should call this immediately after calling
     * {@link #setTask}. This will wait until the task completes, up to the timeout (which can be
     * set with {@link #setTimeout}).
     */
    protected void waitForCompletion() {
        /*
         * onServiceConnected() is always called on the main thread, and we block the current thread
         * for up to 10 seconds as a timeout. If we're currently on the main thread,
         * onServiceConnected() is not called until our timeout elapses (and the UI is frozen for
         * the duration).
         */
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("This cannot be called on the main thread.");
        }

        synchronized (mConnection) {
            long time = System.currentTimeMillis();
            try {
                if (DEBUG_PROXY) {
                    mLogger.debug("Waiting for task " + mName + " to complete...");
                }
                mConnection.wait(mTimeout * 1000L);
            } catch (InterruptedException e) {
                // Can be ignored safely
            }
            if (DEBUG_PROXY) {
                mLogger.debug("Wait for " + mName +
                        (mTaskCompleted ? " finished in " : " timed out in ") +
                        (System.currentTimeMillis() - time) + "ms");
            }
        }
    }

    /**
     * Connection test; return indicates whether the remote service can be connected to
     *
     * @return the result of trying to connect to the remote service
     */
    public boolean test() {
        try {
            return setTask(new ProxyTask() {
                @Override
                public void run() throws RemoteException {
                    if (DEBUG_PROXY) {
                        mLogger.debug("Connection test succeeded in " +
                                (System.currentTimeMillis() - mStartTime) + "ms");
                    }
                }
            }, "test");
        } catch (Exception e) {
            // For any failure, return false.
            return false;
        }
    }

    protected interface ProxyTask {
        public void run() throws RemoteException;
    }

    private class ProxyConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (DEBUG_PROXY) {
                mLogger.debug("Connected: " + name.getShortClassName() + " at " +
                        (System.currentTimeMillis() - mStartTime) + "ms");
            }

            // Let subclasses handle the binder.
            onConnected(binder);

            // Do our work in another thread.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        mTask.run();
                    } catch (RemoteException e) {
                        mLogger.debug("RemoteException thrown running mTask!");
                    } finally {
                        // Make sure that we unbind the mConnection even on exceptions in the
                        // task provided by the subclass.
                        try {
                            // Each ServiceProxy handles just one task, so we unbind after we're
                            // done with our work.
                            mContext.unbindService(mConnection);
                        } catch (RuntimeException e) {
                            // The exceptions that are thrown here look like IllegalStateException,
                            // IllegalArgumentException and RuntimeException. Catching
                            // RuntimeException which get them all. Reasons for these exceptions
                            // include services that have already been stopped or unbound. This can
                            // happen if the user ended the activity that was using the service.
                            // This is harmless, but we've got to catch it.
                            mLogger.debug("RuntimeException when trying to unbind from service");
                        }
                    }
                    mTaskCompleted = true;
                    synchronized (mConnection) {
                        if (DEBUG_PROXY) {
                            mLogger.debug("Task " + mName + " completed; disconnecting");
                        }
                        mConnection.notify();
                    }
                    return null;
                }
            }.execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG_PROXY) {
                mLogger.debug("Disconnected: " + name.getShortClassName() + " at " +
                        (System.currentTimeMillis() - mStartTime) + "ms");
            }
        }
    }
}
