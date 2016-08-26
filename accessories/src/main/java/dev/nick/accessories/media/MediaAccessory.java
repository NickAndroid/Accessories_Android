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

package dev.nick.accessories.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dev.nick.accessories.common.annotation.AccessoryApi;
import dev.nick.accessories.common.annotation.Lazy;
import dev.nick.accessories.common.annotation.Shared;
import dev.nick.accessories.media.cache.BitmapCacheManager;
import dev.nick.accessories.media.cache.CacheManager;
import dev.nick.accessories.media.cache.MovieCacheManager;
import dev.nick.accessories.media.control.Forkable;
import dev.nick.accessories.media.control.Freezer;
import dev.nick.accessories.media.control.LoaderState;
import dev.nick.accessories.media.control.StorageStats;
import dev.nick.accessories.media.control.TrafficStats;
import dev.nick.accessories.media.queue.FIFOPriorityBlockingQueue;
import dev.nick.accessories.media.queue.IdleStateMonitor;
import dev.nick.accessories.media.queue.LIFOPriorityBlockingQueue;
import dev.nick.accessories.media.queue.Priority;
import dev.nick.accessories.media.queue.QueuePolicy;
import dev.nick.accessories.media.queue.RequestHandler;
import dev.nick.accessories.media.queue.RequestQueueManager;
import dev.nick.accessories.media.scrollable.AbsListViewScrollDetector;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.IDCreator;
import dev.nick.accessories.media.ui.IDCreatorImpl;
import dev.nick.accessories.media.ui.ImageViewDelegate;
import dev.nick.accessories.media.ui.MediaHolder;
import dev.nick.accessories.media.ui.MediaQuality;
import dev.nick.accessories.media.utils.Preconditions;
import dev.nick.accessories.media.worker.DimenSpec;
import dev.nick.accessories.media.worker.MediaData;
import dev.nick.accessories.media.worker.MediaSource;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.bitmap.BitmapMediaSource;
import dev.nick.accessories.media.worker.movie.MovieMediaSource;
import dev.nick.accessories.media.worker.result.ErrorListener;
import dev.nick.accessories.media.worker.task.BaseFutureTask;
import dev.nick.accessories.media.worker.task.BitmapDisplayTask;
import dev.nick.accessories.media.worker.task.DisplayTaskRecord;
import dev.nick.accessories.media.worker.task.FutureBitmapTask;
import dev.nick.accessories.media.worker.task.FutureMovieTask;
import dev.nick.accessories.media.worker.task.MokeFutureImageTask;
import dev.nick.accessories.media.worker.task.MovieDisplayTask;
import dev.nick.accessories.media.worker.task.TaskManager;
import dev.nick.accessories.media.worker.task.TaskManagerImpl;
import dev.nick.accessories.logger.Logger;
import dev.nick.accessories.logger.LoggerManager;
import lombok.Getter;
import lombok.Synchronized;

/**
 * Main class of {@link MediaAccessory} library.
 */
public class MediaAccessory implements
        BaseFutureTask.TaskActionListener,
        Forkable<MediaAccessory, AccessoryConfig>,
        Terminable {

    @Lazy
    private static RecyclerView.OnScrollListener sRecyclerViewListener;
    @Lazy
    private static AbsListViewScrollDetector sListViewScrollDetector;

    @Shared
    private static MediaAccessory sAccessory;

    private final List<BaseFutureTask> mFutures;
    /*package*/ RequestQueueManager<Transaction> mTransactionService;
    private Context mContext;
    private UIThreadRouter mUiThreadRouter;
    private UISettingApplier mUISettingApplier;
    @Lazy
    private CacheManager<Bitmap> mBitmapCacheManager;
    @Lazy
    private CacheManager<Movie> mMovieCacheManager;
    private AccessoryConfig mConfig;
    private RequestQueueManager<BaseFutureTask> mTaskHandleService;
    private Logger mLogger;

    private ThreadPoolExecutor mLoadingService;
    @Lazy
    private ThreadPoolExecutor mFallbackService;

    private Freezer mFreezer;

    @Getter
    private LoaderState mState;

    private TaskManager mTaskManager;
    private IDCreator mSettableIdCreator;

    @SuppressLint("DefaultLocale")
    private MediaAccessory(Context context, AccessoryConfig config) {
        Preconditions.checkNotNull(context);
        if (config == null) config = AccessoryConfig.DEFAULT_CONFIG;
        LoggerManager.setDebugLevel(config.getDebugLevel());
        this.mContext = context;
        this.mConfig = config;
        this.mTaskManager = new TaskManagerImpl();
        this.mSettableIdCreator = new IDCreatorImpl();
        this.mUiThreadRouter = UIThreadRouter.getSharedRouter();
        this.mUISettingApplier = UISettingApplier.getSharedApplier();
        //noinspection deprecation
        this.mLoadingService = new ThreadPoolExecutor(
                config.getLoadingThreads(),
                config.getLoadingThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                config.getQueuePolicy() == QueuePolicy.FIFO
                        ? new FIFOPriorityBlockingQueue<Runnable>()
                        : new LIFOPriorityBlockingQueue<Runnable>());
        int loaderId = AccessoryFactory.assignId();
        this.mTaskHandleService = RequestQueueManager.createStarted(new TaskHandler(), null, null, "TaskHandleService#" + loaderId);
        this.mTransactionService = RequestQueueManager.createStarted(new TransactionHandler(), new IdleStateMonitor() {
            @Override
            public void onIdle() {
                LoggerManager.getLogger(IdleStateMonitor.class).funcEnter();
                TrafficStats.from(mContext).flush();
                StorageStats.from(mContext).flush();
            }
        }, QueuePolicy.FIFO, "TransactionService#" + loaderId);
        this.mFutures = new ArrayList<>();
        this.mState = LoaderState.RUNNING;
        this.mLogger = LoggerManager.getLogger(getClass().getSimpleName() + "#" + loaderId);
        this.mLogger.verbose(String.format("Create accessory-%d with config %s", loaderId, config));
    }

    @SuppressLint("DefaultLocale")
    private MediaAccessory(MediaAccessory from, AccessoryConfig config) {
        Preconditions.checkNotNull(from);
        if (config == null) config = AccessoryConfig.DEFAULT_CONFIG;
        LoggerManager.setDebugLevel(config.getDebugLevel());
        this.mContext = from.mContext;
        this.mConfig = config;
        this.mTaskManager = new TaskManagerImpl();
        this.mSettableIdCreator = from.mSettableIdCreator;
        this.mUiThreadRouter = UIThreadRouter.getSharedRouter();
        this.mUISettingApplier = UISettingApplier.getSharedApplier();
        this.mBitmapCacheManager = from.lazyGetBitmapCacheManager().fork(config.getCachePolicy());
        this.mMovieCacheManager = from.lazyGetMovieCacheManager().fork(config.getCachePolicy());
        //noinspection deprecation
        this.mLoadingService = new ThreadPoolExecutor(
                config.getLoadingThreads(),
                config.getLoadingThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                config.getQueuePolicy() == QueuePolicy.FIFO
                        ? new FIFOPriorityBlockingQueue<Runnable>()
                        : new LIFOPriorityBlockingQueue<Runnable>());
        int loaderId = AccessoryFactory.assignId();
        this.mTaskHandleService = RequestQueueManager.createStarted(new TaskHandler(), null, null, "TaskHandleService#" + loaderId);
        this.mTransactionService = RequestQueueManager.createStarted(new TransactionHandler(), new IdleStateMonitor() {
            @Override
            public void onIdle() {
                LoggerManager.getLogger(IdleStateMonitor.class).funcEnter();
                TrafficStats.from(mContext).flush();
                StorageStats.from(mContext).flush();
            }
        }, QueuePolicy.FIFO, "TransactionService#" + loaderId);
        this.mFutures = new ArrayList<>();
        this.mState = LoaderState.RUNNING;
        this.mLogger = LoggerManager.getLogger(getClass().getSimpleName() + "#" + loaderId);
        this.mLogger.verbose(String.format("Create accessory-%d with config %s", loaderId, config));
    }

    @AccessoryApi
    private static MediaAccessory clone(MediaAccessory from, AccessoryConfig config) {
        return new MediaAccessory(from, config);
    }

    /**
     * Create the shared instance of MediaAccessory
     *
     * @param context An application {@link Context} is preferred.
     * @since 1.0.1
     */
    @AccessoryApi
    public static void createShared(Context context) {
        createShared(context, null);
    }

    /**
     * Create the shared instance of MediaAccessory
     *
     * @param context An application {@link Context} is preferred.
     * @param config  Configuration of this accessory.
     * @since 1.0.1
     */
    @AccessoryApi
    public static void createShared(Context context, AccessoryConfig config) {
        if (sAccessory == null || sAccessory.isTerminated()) {
            sAccessory = new MediaAccessory(context, config);
        }
    }

    /**
     * Get the createShared instance of MediaAccessory
     *
     * @return Single instance of {@link MediaAccessory}
     * @since 1.0.1
     */
    @AccessoryApi
    public static MediaAccessory shared() {
        return Preconditions.checkNotNull(sAccessory, "Call MediaAccessory#createShared first");
    }

    /**
     * Start a quick optional transaction builder,
     * do not forget to call {@link Transaction#start()} to start this task.
     *
     * @return An optional params wrapper.
     * @see Transaction
     */
    @AccessoryApi
    public BitmapTransaction loadBitmap() {
        return new BitmapTransaction(this);
    }

    /**
     * Start a quick optional transaction builder,
     * do not forget to call {@link Transaction#start()} to start this task.
     *
     * @return An optional params wrapper.
     * @see Transaction
     */
    @AccessoryApi
    public MovieTransaction loadMovie() {
        return new MovieTransaction(this);
    }

    Future<Bitmap> displayBitmap(@NonNull MediaData<Bitmap> mediaData,
                                 @NonNull MediaHolder<Bitmap> mediaHolder,
                                 @NonNull DisplayOption<Bitmap> option,
                                 @Nullable ProgressListener<Bitmap> progressListener,
                                 @Nullable ErrorListener errorListener,
                                 @Nullable Priority priority) {

        ensureNotTerminated();

        if (isPaused()) return null;

        Preconditions.checkNotNull(mediaData.getUrl(), "mediaData is null");

        DisplayTaskRecord record = createTaskRecord(Preconditions.checkNotNull(mediaHolder));

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        lazyGetBitmapCacheManager();

        MediaQuality mediaQuality = option.getQuality();

        DimenSpec dimenSpec = new DimenSpec(mediaHolder.getWidth(), mediaHolder.getHeight());

        ProgressListenerDelegate<Bitmap> progressListenerDelegate = new BitmapProgressListenerDelegate(
                mBitmapCacheManager,
                mTaskManager,
                progressListener,
                dimenSpec,
                option,
                mediaHolder,
                record,
                mediaData.getUrl());

        if (mBitmapCacheManager.isMemCacheEnabled()) {
            Bitmap cached;
            if ((cached = mBitmapCacheManager.get(mediaData.getUrl())) != null) {
                mLogger.verbose("Using mem cached bitmap for:" + mediaData.getUrl());
                mUISettingApplier.applySettings(
                        cached,
                        option.getMediaArts(),
                        mediaHolder,
                        option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                progressListenerDelegate.callOnComplete(cached);
                return new MokeFutureImageTask<>(cached);
            }
        }

        String loadingUrl = mediaData.getUrl();
        boolean usingDiskCacheUrl = false;

        if (mBitmapCacheManager.isDiskCacheEnabled()) {
            String cachePath;
            if ((cachePath = mBitmapCacheManager.getCachePath(loadingUrl)) != null) {
                loadingUrl = BitmapMediaSource.FILE.getPrefix() + cachePath;
                // Check mem cache again.
                if (mBitmapCacheManager.isMemCacheEnabled()) {
                    Bitmap cached;
                    if ((cached = mBitmapCacheManager.get(loadingUrl)) != null) {
                        mLogger.verbose("Using mem cached bitmap for:" + mediaData.getUrl());
                        mUISettingApplier.applySettings(
                                cached,
                                option.getMediaArts(),
                                mediaHolder,
                                option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                        progressListenerDelegate.callOnComplete(cached);
                        return new MokeFutureImageTask<>(cached);
                    }
                }
                mediaData.setUrl(loadingUrl);
                mediaData.setSource(BitmapMediaSource.FILE);
                usingDiskCacheUrl = true;
            }
        }

        showOnLoadingBm(mediaHolder, option);

        ErrorListenerDelegate<Bitmap> errorListenerDelegate = new BitmapErrorListenerDelegate(
                errorListener,
                option.isFailureImgDefined() ? option.getFailureImg() : null,
                mediaHolder);

        if (usingDiskCacheUrl) {
            mLogger.verbose("Using disk cache url for loading:" + loadingUrl);
        } else {
            mLogger.verbose("No cache found, perform loading: " + loadingUrl);
        }

        BitmapDisplayTask imageTask = new BitmapDisplayTask(
                mContext,
                mConfig,
                mTaskManager,
                mediaData,
                dimenSpec,
                mediaQuality,
                progressListenerDelegate,
                errorListenerDelegate,
                record);

        FutureBitmapTask future = new FutureBitmapTask(imageTask, this, option.isViewMaybeReused());
        future.setPriority(priority == null ? Priority.NORMAL : priority);

        mTaskHandleService.push(future);

        return future;
    }

    Future<Movie> displayMovie(@NonNull MediaData<Movie> source,
                               @NonNull MediaHolder<Movie> mediaHolder,
                               @NonNull DisplayOption<Movie> option,
                               @Nullable ProgressListener<Movie> progressListener,
                               @Nullable ErrorListener errorListener,
                               @Nullable Priority priority) {
        ensureNotTerminated();

        if (isPaused()) return null;

        Preconditions.checkNotNull(source.getUrl(), "mediaData is null");

        DisplayTaskRecord record = createTaskRecord(Preconditions.checkNotNull(mediaHolder));

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        lazyGetMovieCacheManager();

        MediaQuality mediaQuality = option.getQuality();
        DimenSpec dimenSpec = new DimenSpec(mediaHolder.getWidth(), mediaHolder.getHeight());

        ProgressListenerDelegate<Movie> progressListenerDelegate = new MovieProgressListenerDelegate(
                mMovieCacheManager,
                mTaskManager,
                progressListener,
                dimenSpec,
                option,
                mediaHolder,
                record,
                source.getUrl());

        if (mMovieCacheManager.isMemCacheEnabled()) {
            Movie cached;
            if ((cached = mMovieCacheManager.get(source.getUrl())) != null) {
                mUISettingApplier.applySettings(
                        cached,
                        option.getMediaArts(),
                        mediaHolder,
                        option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                progressListenerDelegate.callOnComplete(cached);
                return new MokeFutureImageTask<>(cached);
            }
        }

        String loadingUrl = source.getUrl();
        boolean usingDiskCacheUrl = false;

        if (mMovieCacheManager.isDiskCacheEnabled()) {
            String cachePath;
            if ((cachePath = mMovieCacheManager.getCachePath(loadingUrl)) != null) {
                loadingUrl = BitmapMediaSource.FILE.getPrefix() + cachePath;
                // Check mem cache again.
                if (mMovieCacheManager.isMemCacheEnabled()) {
                    Movie cached;
                    if ((cached = mMovieCacheManager.get(loadingUrl)) != null) {
                        mUISettingApplier.applySettings(
                                cached,
                                option.getMediaArts(),
                                mediaHolder,
                                option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                        progressListenerDelegate.callOnComplete(cached);
                        return new MokeFutureImageTask<>(cached);
                    }
                }
                source.setUrl(loadingUrl);
                source.setSource(MovieMediaSource.FILE);
                usingDiskCacheUrl = true;
            }
        }

        if (usingDiskCacheUrl) {
            mLogger.verbose("Using disk cache url for loading:" + loadingUrl);
        } else {
            mLogger.verbose("No cache found, perform loading: " + loadingUrl);
        }

        showOnLoadingMov(mediaHolder, option);

        ErrorListenerDelegate errorListenerDelegate = new MovieErrorListenerDelegate(
                errorListener,
                option.isFailureImgDefined() ? option.getFailureImg() : null,
                mediaHolder);

        MovieDisplayTask imageTask = new MovieDisplayTask(
                mContext,
                mConfig,
                mTaskManager,
                source,
                dimenSpec,
                mediaQuality,
                progressListenerDelegate,
                errorListenerDelegate,
                record);

        FutureMovieTask future = new FutureMovieTask(imageTask, this, option.isViewMaybeReused());
        future.setPriority(priority == null ? Priority.NORMAL : priority);

        mTaskHandleService.push(future);

        return future;
    }

    private synchronized CacheManager<Bitmap> lazyGetBitmapCacheManager() {
        if (mBitmapCacheManager == null)
            this.mBitmapCacheManager = new BitmapCacheManager(mConfig.getCachePolicy(), mContext);
        return mBitmapCacheManager;
    }

    private synchronized CacheManager<Movie> lazyGetMovieCacheManager() {
        if (mMovieCacheManager == null)
            this.mMovieCacheManager = new MovieCacheManager(mConfig.getCachePolicy(), mContext);
        return mMovieCacheManager;
    }

    private DisplayTaskRecord createTaskRecord(MediaHolder settable) {
        long settableId = mSettableIdCreator.createSettableId(settable);
        int taskId = mTaskManager.nextTaskId();
        DisplayTaskRecord displayTaskRecord = new DisplayTaskRecord(settableId, taskId);
        mTaskManager.onDisplayTaskCreated(displayTaskRecord);
        return displayTaskRecord;
    }

    private void showOnLoadingBm(MediaHolder<Bitmap> settable, DisplayOption<Bitmap> option) {
        if (option.isLoadingImgDefined()) {
            Bitmap showWhenLoading = option.getLoadingImg();
            mUISettingApplier.applySettings(showWhenLoading, null, settable, null);
        }
    }

    private void showOnLoadingMov(MediaHolder<Movie> settable, DisplayOption option) {
        if (option.isLoadingImgDefined()) {
            // TODO: 16-8-21 Impl
        }
    }

    private boolean onFutureSubmit(BaseFutureTask futureTask) {
        if (futureTask.shouldCancelOthersBeforeRun()) {
            cancel(futureTask.getListenableTask().getTaskRecord().getViewId());
        }
        synchronized (mFutures) {
            mFutures.add(futureTask);
        }
        return true;
    }

    private void onFutureDone(BaseFutureTask task) {
        synchronized (mFutures) {
            mFutures.remove(task);
        }
    }

    private void onFutureCancel(BaseFutureTask task) {
        synchronized (mFutures) {
            mFutures.remove(task);
        }
        mUiThreadRouter.callOnCancel(task.getListenableTask().getProgressListener());
    }

    private ExecutorService getExecutor(MediaSource type) {
        if (type.maybeSlow()) {
            mLogger.verbose("Using default loading service for slower task.");
            return mLoadingService;
        } else {
            int activeThreads = mLoadingService.getActiveCount();
            int max = mLoadingService.getMaximumPoolSize();
            if (activeThreads == max) {
                mLogger.warn("The loading service hits, using fallback one.");
                ensureFallbackService();
                return mFallbackService;
            }
        }
        mLogger.verbose("Using default loading service.");
        return mLoadingService;
    }

    @SuppressWarnings("deprecation")
    private synchronized void ensureFallbackService() {
        if (mFallbackService == null) {
            int poolSize = mConfig.getLoadingThreads();
            poolSize = poolSize / 2 + 1;
            this.mFallbackService = new ThreadPoolExecutor(
                    poolSize,
                    poolSize,
                    0L,
                    TimeUnit.MILLISECONDS,
                    mConfig.getQueuePolicy() == QueuePolicy.FIFO
                            ? new FIFOPriorityBlockingQueue<Runnable>()
                            : new LIFOPriorityBlockingQueue<Runnable>());
            mLogger.verbose("Created fallback service with pool size:" + poolSize);
        }
    }

    public
    @AccessoryApi
    @NonNull
    AbsListViewScrollDetector linkScrollStateTo(@NonNull AbsListView view) {
        mLogger.verbose(view);
        synchronized (this) {
            if (sListViewScrollDetector == null) {
                sListViewScrollDetector = new AbsListViewScrollDetector() {
                    @Override
                    public void onScrollUp() {
                        pause();
                        cancelAllTasks();
                    }

                    @Override
                    public void onScrollDown() {
                        pause();
                        cancelAllTasks();
                    }

                    @Override
                    public void onIdle() {
                        resume();
                    }
                };
            }
        }
        Preconditions.checkNotNull(view).setOnScrollListener(sListViewScrollDetector);
        sListViewScrollDetector.setListView(view);
        return sListViewScrollDetector;
    }

    @AccessoryApi
    public void unLinkScrollStateTo(@NonNull AbsListView view) {
        mLogger.verbose(view);
        view.setOnScrollListener(null);
    }

    public
    @AccessoryApi
    void linkScrollStateTo(@NonNull RecyclerView view) {
        mLogger.verbose(view);
        synchronized (this) {
            if (sRecyclerViewListener == null) {
                sRecyclerViewListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            resume();
                        } else {
                            pause();
                            cancelAllTasks();
                        }
                    }
                };
            }
        }
        view.addOnScrollListener(sRecyclerViewListener);
    }

    @AccessoryApi
    public void unLinkScrollStateTo(@NonNull RecyclerView view) {
        mLogger.verbose(view);
        view.removeOnScrollListener(sRecyclerViewListener);
    }

    /**
     * Call this to pause the {@link MediaAccessory}
     */
    @AccessoryApi
    public void pause() {
        ensureNotTerminated();
        if (!isPaused()) {
            mState = LoaderState.PAUSE_REQUESTED;
            mLogger.funcExit();
        }
    }

    /**
     * @return {@code true} if this accessory is paused.
     * @see #pause()
     */
    @AccessoryApi
    public boolean isPaused() {
        synchronized (this) {
            return mState == LoaderState.PAUSED || mState == LoaderState.PAUSE_REQUESTED;
        }
    }

    /**
     * @return {@code true} if this accessory is terminated.
     * @see #terminate() ()
     */
    @AccessoryApi
    public boolean isTerminated() {
        synchronized (this) {
            return mState == LoaderState.TERMINATED;
        }
    }

    /**
     * Call this to resume the {@link MediaAccessory} from pause state.
     *
     * @see #pause()
     * @see LoaderState
     */
    @AccessoryApi
    public void resume() {
        ensureNotTerminated();
        synchronized (this) {
            if (isPaused()) {
                if (mFreezer != null) {
                    mFreezer.resume();
                }
                mState = LoaderState.RUNNING;
                mLogger.funcExit();
            }
        }
    }

    /**
     * Clear all pending tasks.
     */
    @AccessoryApi
    public void clearTasks() {
        ensureNotTerminated();
        mTaskManager.clearTasks();
    }

    /**
     * Terminate the accessory.
     */
    @AccessoryApi
    @Override
    @Synchronized
    public void terminate() {
        ensureNotTerminated();
        mState = LoaderState.TERMINATED;
        mTaskHandleService.terminate();
        mLoadingService.shutdown();
        mTaskManager.terminate();
        cancelAllTasks();
        mLogger.funcExit();
    }

    private void ensureNotTerminated() {
        Preconditions.checkState(!isTerminated(), "Loader has already been terminated");
    }

    /**
     * Clear all the load and loading tasks.
     */
    @AccessoryApi
    public void cancelAllTasks() {
        List<BaseFutureTask> tasks = new ArrayList<>(mFutures);
        for (BaseFutureTask task : tasks) {
            task.cancel(true);
        }
    }

    /**
     * Cancel the load and loading task who's from match given.
     *
     * @param url The from of the accessory request.
     */
    @AccessoryApi
    public MediaAccessory cancel(@NonNull String url) {
        Preconditions.checkNotNull(url);
        List<BaseFutureTask> pendingCancels = findTasks(url);
        if (pendingCancels.size() > 0) {
            for (BaseFutureTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                mLogger.info("Cancel task for from:" + url);
            }
            pendingCancels.clear();
        }
        return this;
    }

    /**
     * Cancel the load and loading task who's view match given.
     *
     * @param view The view of the accessory request.
     */
    @AccessoryApi
    public MediaAccessory cancel(@NonNull ImageView view) {
        return cancel(new ImageViewDelegate(view));
    }

    /**
     * Cancel the load and loading task who's settable match given.
     *
     * @param settable The settable of the accessory request.
     */
    @AccessoryApi
    public MediaAccessory cancel(@NonNull MediaHolder settable) {
        return cancel(mSettableIdCreator.createSettableId(settable));
    }

    /**
     * Cancel the load and loading task who's settableId match given.
     *
     * @param settableId The settableId of the accessory request.
     */
    @AccessoryApi
    public MediaAccessory cancel(long settableId) {
        Preconditions.checkState(settableId != 0, "Invalid settable with id:0");
        List<BaseFutureTask> pendingCancels = findTasks(settableId);
        if (pendingCancels.size() > 0) {
            for (BaseFutureTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                mLogger.verbose("Cancel task for settable:" + settableId);
            }
            pendingCancels.clear();
        }
        return this;
    }

    private List<BaseFutureTask> findTasks(@NonNull String url) {
        List<BaseFutureTask> pendingCancels = new ArrayList<>();
        synchronized (mFutures) {
            for (BaseFutureTask futureTask : mFutures) {
                if (!futureTask.isCancelled()
                        && !futureTask.isDone()
                        && url.equals(futureTask.getListenableTask().getImageData().getUrl())) {
                    pendingCancels.add(futureTask);
                }
            }
        }
        return pendingCancels;
    }

    private List<BaseFutureTask> findTasks(long settableId) {
        List<BaseFutureTask> pendingCancels = new ArrayList<>();
        synchronized (mFutures) {
            for (BaseFutureTask futureTask : mFutures) {
                if (!futureTask.isCancelled()
                        && !futureTask.isDone()
                        && settableId == futureTask.getListenableTask().getTaskRecord().getViewId()) {
                    pendingCancels.add(futureTask);
                }
            }
        }
        return pendingCancels;
    }

    @WorkerThread
    public void clearAllCache() {
        clearDiskCache();
        clearMemCache();
        mLogger.funcExit();
    }

    @WorkerThread
    public void clearDiskCache() {
        mBitmapCacheManager.evictDisk();
        StorageStats.from(mContext).reset();
        mLogger.funcExit();
    }

    @WorkerThread
    public void clearMemCache() {
        mBitmapCacheManager.evictMem();
        mLogger.funcExit();
    }

    void freezeIfRequested() {
        if (mState == LoaderState.PAUSE_REQUESTED) {
            mState = LoaderState.PAUSED;
            if (mFreezer == null) mFreezer = new Freezer();
            mLogger.debug("Freezing the accessory...");
            mFreezer.freeze();
        }
    }

    @Override
    public void onDone(BaseFutureTask futureTask) {
        mLogger.verbose(futureTask.getListenableTask().getTaskRecord());
        onFutureDone(futureTask);
    }

    @Override
    public void onCancel(BaseFutureTask futureTask) {
        mLogger.verbose(futureTask.getListenableTask().getTaskRecord());
        onFutureCancel(futureTask);
    }

    @Override
    public MediaAccessory fork(AccessoryConfig param) {
        return clone(this, param);
    }

    @AccessoryApi
    public long getInternalStorageUsage() {
        return StorageStats.from(mContext).getInternalStorageUsage();
    }

    @AccessoryApi
    public long getExternalStorageUsage() {
        return StorageStats.from(mContext).getExternalStorageUsage();
    }

    @AccessoryApi
    public long getTotalStorageUsage() {
        return StorageStats.from(mContext).getTotalStorageUsage();
    }

    @AccessoryApi
    public long getTotalTrafficUsage() {
        return TrafficStats.from(mContext).getTotalTrafficUsage();
    }

    @AccessoryApi
    public long getMobileTrafficUsage() {
        return TrafficStats.from(mContext).getMobileTrafficUsage();
    }

    @AccessoryApi
    public long getWifiTrafficUsage() {
        return TrafficStats.from(mContext).getWifiTrafficUsage();
    }

    private class TaskHandler implements RequestHandler<BaseFutureTask> {
        @Override
        public boolean handleRequest(BaseFutureTask request) {
            freezeIfRequested();
            if (!onFutureSubmit(request)) return false;
            mLogger.funcEnter();
            getExecutor(request.getListenableTask().getImageData().getSource()).submit(request);
            return true;
        }
    }

    private class TransactionHandler implements RequestHandler<Transaction> {

        @Override
        public boolean handleRequest(Transaction transaction) {
            transaction.startAsync();
            return true;
        }
    }

}
