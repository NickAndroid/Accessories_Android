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

package dev.nick.imageloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.AbsListView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dev.nick.imageloader.annotation.Lazy;
import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.annotation.Shared;
import dev.nick.imageloader.cache.BitmapCacheManager;
import dev.nick.imageloader.cache.CacheManager;
import dev.nick.imageloader.cache.MovieCacheManager;
import dev.nick.imageloader.control.Forkable;
import dev.nick.imageloader.control.Freezer;
import dev.nick.imageloader.control.LoaderState;
import dev.nick.imageloader.control.StorageStats;
import dev.nick.imageloader.control.TrafficStats;
import dev.nick.imageloader.debug.Logger;
import dev.nick.imageloader.debug.LoggerManager;
import dev.nick.imageloader.queue.FIFOPriorityBlockingQueue;
import dev.nick.imageloader.queue.IdleStateMonitor;
import dev.nick.imageloader.queue.LIFOPriorityBlockingQueue;
import dev.nick.imageloader.queue.Priority;
import dev.nick.imageloader.queue.QueuePolicy;
import dev.nick.imageloader.queue.RequestHandler;
import dev.nick.imageloader.queue.RequestQueueManager;
import dev.nick.imageloader.scrollable.AbsListViewScrollDetector;
import dev.nick.imageloader.ui.BitmapImageViewDelegate;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageChair;
import dev.nick.imageloader.ui.ImageQuality;
import dev.nick.imageloader.ui.ImageSettableIdCreator;
import dev.nick.imageloader.ui.ImageSettableIdCreatorImpl;
import dev.nick.imageloader.utils.Preconditions;
import dev.nick.imageloader.worker.DimenSpec;
import dev.nick.imageloader.worker.ImageData;
import dev.nick.imageloader.worker.ImageSource;
import dev.nick.imageloader.worker.ProgressListener;
import dev.nick.imageloader.worker.bitmap.BitmapImageSource;
import dev.nick.imageloader.worker.result.ErrorListener;
import dev.nick.imageloader.worker.task.BaseFutureTask;
import dev.nick.imageloader.worker.task.BitmapDisplayTask;
import dev.nick.imageloader.worker.task.DisplayTaskRecord;
import dev.nick.imageloader.worker.task.FutureBitmapTask;
import dev.nick.imageloader.worker.task.FutureMovieTask;
import dev.nick.imageloader.worker.task.MokeFutureImageTask;
import dev.nick.imageloader.worker.task.MovieDisplayTask;
import dev.nick.imageloader.worker.task.TaskManager;
import dev.nick.imageloader.worker.task.TaskManagerImpl;

/**
 * Main class of {@link ImageLoader} library.
 */
public class ImageLoader implements
        BaseFutureTask.TaskActionListener,
        Forkable<ImageLoader, LoaderConfig>,
        Terminable {

    private static final DisplayOption<Bitmap> sDefDisplayOption = DisplayOption.bitmapBuilder()
            .imageQuality(ImageQuality.OPT)
            .viewMaybeReused()
            .build();

    private static final DisplayOption<Movie> sDefDisplayOptionMovie = DisplayOption.movieBuilder()
            .imageQuality(ImageQuality.OPT)
            .viewMaybeReused()
            .build();

    @Shared
    private static ImageLoader sLoader;

    private final List<BaseFutureTask> mFutures;

    private Context mContext;

    private UIThreadRouter mUiThreadRouter;
    private ImageSettingApplier mImageSettingApplier;

    @Lazy
    private CacheManager<Bitmap> mBitmapCacheManager;
    @Lazy
    private CacheManager<Movie> mMovieCacheManager;

    private LoaderConfig mConfig;
    private RequestQueueManager<BaseFutureTask> mTaskHandleService;
    private RequestQueueManager<Transaction> mTransactionService;

    private Logger mLogger;

    private ThreadPoolExecutor mLoadingService;
    @Lazy
    private ThreadPoolExecutor mFallbackService;

    private Freezer mFreezer;
    private LoaderState mState;

    private TaskManager mTaskManager;
    private ImageSettableIdCreator mSettableIdCreator;

    @SuppressLint("DefaultLocale")
    private ImageLoader(Context context, LoaderConfig config) {
        Preconditions.checkNotNull(context);
        if (config == null) config = LoaderConfig.DEFAULT_CONFIG;
        LoggerManager.setDebugLevel(config.getDebugLevel());
        this.mContext = context;
        this.mConfig = config;
        this.mTaskManager = new TaskManagerImpl();
        this.mSettableIdCreator = new ImageSettableIdCreatorImpl();
        this.mUiThreadRouter = UIThreadRouter.getSharedRouter();
        this.mImageSettingApplier = ImageSettingApplier.getSharedApplier();
        //noinspection deprecation
        this.mLoadingService = new ThreadPoolExecutor(
                config.getLoadingThreads(),
                config.getLoadingThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                config.getQueuePolicy() == QueuePolicy.FIFO
                        ? new FIFOPriorityBlockingQueue<Runnable>()
                        : new LIFOPriorityBlockingQueue<Runnable>());
        int loaderId = LoaderFactory.assignLoaderId();
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
        this.mLogger.verbose(String.format("Create loader-%d with config %s", loaderId, config));
    }


    @SuppressLint("DefaultLocale")
    private ImageLoader(ImageLoader from, LoaderConfig config) {
        Preconditions.checkNotNull(from);
        if (config == null) config = LoaderConfig.DEFAULT_CONFIG;
        LoggerManager.setDebugLevel(config.getDebugLevel());
        this.mContext = from.mContext;
        this.mConfig = config;
        this.mTaskManager = new TaskManagerImpl();
        this.mSettableIdCreator = from.mSettableIdCreator;
        this.mUiThreadRouter = UIThreadRouter.getSharedRouter();
        this.mImageSettingApplier = ImageSettingApplier.getSharedApplier();
        this.mBitmapCacheManager = from.lazyGetBitmapCacheManager();
        this.mMovieCacheManager = from.lazyGetMovieCacheManager();
        //noinspection deprecation
        this.mLoadingService = new ThreadPoolExecutor(
                config.getLoadingThreads(),
                config.getLoadingThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                config.getQueuePolicy() == QueuePolicy.FIFO
                        ? new FIFOPriorityBlockingQueue<Runnable>()
                        : new LIFOPriorityBlockingQueue<Runnable>());
        int loaderId = LoaderFactory.assignLoaderId();
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
        this.mLogger.verbose(String.format("Create loader-%d with config %s", loaderId, config));
    }

    @LoaderApi
    private static ImageLoader clone(ImageLoader from, LoaderConfig config) {
        return new ImageLoader(from, config);
    }

    /**
     * Create the shared instance of ImageLoader
     *
     * @param context An application {@link Context} is preferred.
     * @since 1.0.1
     */
    @LoaderApi
    public static void createShared(Context context) {
        createShared(context, null);
    }

    /**
     * Create the shared instance of ImageLoader
     *
     * @param context An application {@link Context} is preferred.
     * @param config  Configuration of this loader.
     * @since 1.0.1
     */
    @LoaderApi
    public static void createShared(Context context, LoaderConfig config) {
        if (sLoader == null || sLoader.isTerminated()) {
            sLoader = new ImageLoader(context, config);
        }
    }

    /**
     * Get the createShared instance of ImageLoader
     *
     * @return Single instance of {@link ImageLoader}
     * @since 1.0.1
     */
    @LoaderApi
    public static ImageLoader shared() {
        return Preconditions.checkNotNull(sLoader, "Call ImageLoader#createShared first");
    }

    /**
     * Start a quick optional transaction builder,
     * do not forget to call {@link Transaction#start()} to start this task.
     *
     * @return An optional params wrapper.
     * @see Transaction
     */
    @LoaderApi
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
    @LoaderApi
    public MovieTransaction loadMovie() {
        return new MovieTransaction(this);
    }

    /**
     * Display image from the from to the view.
     *
     * @param source           Image source from, one of {@link ImageData}
     * @param imageChair       Target {@link ImageChair} to display the image.
     * @param option           {@link DisplayOption} is options using when display the image.
     * @param progressListener The progress progressListener using to watch the progress of the loading.
     */
    Future<Bitmap> displayBitmap(@NonNull ImageData<Bitmap> source,
                                 @NonNull ImageChair<Bitmap> imageChair,
                                 @Nullable DisplayOption<Bitmap> option,
                                 @Nullable ProgressListener<Bitmap> progressListener,
                                 @Nullable ErrorListener errorListener,
                                 @Nullable Priority priority) {

        ensureNotTerminated();

        Preconditions.checkNotNull(source.getUrl(), "imageData is null");

        DisplayTaskRecord record = createTaskRecord(Preconditions.checkNotNull(imageChair));

        option = assignBitmapOption(option);

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        lazyGetBitmapCacheManager();

        ImageQuality imageQuality = option.getQuality();

        DimenSpec dimenSpec = new DimenSpec(imageChair.getWidth(), imageChair.getHeight());

        ProgressListenerDelegate<Bitmap> progressListenerDelegate = new BitmapProgressListenerDelegate(
                mBitmapCacheManager,
                mTaskManager,
                progressListener,
                dimenSpec,
                option,
                imageChair,
                record,
                source.getUrl());

        if (mBitmapCacheManager.isMemCacheEnabled()) {
            Bitmap cached;
            if ((cached = mBitmapCacheManager.get(source.getUrl())) != null) {
                mLogger.verbose("Using mem cached bitmap for:" + source.getUrl());
                mImageSettingApplier.applyImageSettings(
                        cached,
                        option.getArtist(),
                        imageChair,
                        option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                progressListenerDelegate.callOnComplete(cached);
                return new MokeFutureImageTask<>(cached);
            }
        }

        String loadingUrl = source.getUrl();
        boolean usingDiskCacheUrl = false;

        if (mBitmapCacheManager.isDiskCacheEnabled()) {
            String cachePath;
            if ((cachePath = mBitmapCacheManager.getCachePath(loadingUrl)) != null) {
                loadingUrl = BitmapImageSource.FILE.getPrefix() + cachePath;
                // Check mem cache again.
                if (mBitmapCacheManager.isMemCacheEnabled()) {
                    Bitmap cached;
                    if ((cached = mBitmapCacheManager.get(loadingUrl)) != null) {
                        mLogger.verbose("Using mem cached bitmap for:" + source.getUrl());
                        mImageSettingApplier.applyImageSettings(
                                cached,
                                option.getArtist(),
                                imageChair,
                                option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                        progressListenerDelegate.callOnComplete(cached);
                        return new MokeFutureImageTask<>(cached);
                    }
                }
                source.setUrl(loadingUrl);
                usingDiskCacheUrl = true;
            }
        }

        if (source.getType().maybeSlow()) showOnLoadingBm(imageChair, option);

        ErrorListenerDelegate<Bitmap> errorListenerDelegate = null;

        if (errorListener != null) {
            errorListenerDelegate = new BitmapErrorListenerDelegate(errorListener, option.getFailureImg(), imageChair);
        }

        if (usingDiskCacheUrl) {
            mLogger.verbose("Using disk cache url for loading:" + loadingUrl);
        } else {
            mLogger.verbose("No cache found, perform loading: " + loadingUrl);
        }

        BitmapDisplayTask imageTask = new BitmapDisplayTask(
                mContext,
                mConfig,
                mTaskManager,
                source,
                dimenSpec,
                imageQuality,
                progressListenerDelegate,
                errorListenerDelegate,
                record);

        FutureBitmapTask future = new FutureBitmapTask(imageTask, this, option.isViewMaybeReused());
        future.setPriority(priority == null ? Priority.NORMAL : priority);

        mTaskHandleService.push(future);

        return future;
    }

    Future<Movie> displayMovie(@NonNull ImageData<Movie> source,
                               @NonNull ImageChair<Movie> imageChair,
                               @Nullable DisplayOption<Movie> option,
                               @Nullable ProgressListener<Movie> progressListener,
                               @Nullable ErrorListener errorListener,
                               @Nullable Priority priority) {
        ensureNotTerminated();

        Preconditions.checkNotNull(source.getUrl(), "imageData is null");

        DisplayTaskRecord record = createTaskRecord(Preconditions.checkNotNull(imageChair));

        option = assignMovieOption(option);

        // 1. Get from cache.
        // 2. If no mem cache, start a loading task from disk cache file or perform first loading.
        // 3. Cache the loaded.

        lazyGetMovieCacheManager();

        ImageQuality imageQuality = option.getQuality();
        DimenSpec dimenSpec = new DimenSpec(imageChair.getWidth(), imageChair.getHeight());

        ProgressListenerDelegate<Movie> progressListenerDelegate = new MovieProgressListenerDelegate(
                mMovieCacheManager,
                mTaskManager,
                progressListener,
                dimenSpec,
                option,
                imageChair,
                record,
                source.getUrl());

        if (mMovieCacheManager.isMemCacheEnabled()) {
            Movie cached;
            if ((cached = mMovieCacheManager.get(source.getUrl())) != null) {
                mImageSettingApplier.applyImageSettings(
                        cached,
                        option.getArtist(),
                        imageChair,
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
                loadingUrl = BitmapImageSource.FILE.getPrefix() + cachePath;
                // Check mem cache again.
                if (mMovieCacheManager.isMemCacheEnabled()) {
                    Movie cached;
                    if ((cached = mMovieCacheManager.get(loadingUrl)) != null) {
                        mImageSettingApplier.applyImageSettings(
                                cached,
                                option.getArtist(),
                                imageChair,
                                option.isAnimateOnlyNewLoaded() ? null : option.getAnimator());
                        progressListenerDelegate.callOnComplete(cached);
                        return new MokeFutureImageTask<>(cached);
                    }
                }
                source.setUrl(loadingUrl);
                usingDiskCacheUrl = true;
            }
        }

        if (usingDiskCacheUrl) {
            mLogger.verbose("Using disk cache url for loading:" + loadingUrl);
        } else {
            mLogger.verbose("No cache found, perform loading: " + loadingUrl);
        }

        if (source.getType().maybeSlow()) showOnLoadingMov(imageChair, option);

        ErrorListenerDelegate errorListenerDelegate = null;

        if (errorListener != null) {
            errorListenerDelegate = new MovieErrorListenerDelegate(errorListener, option.getFailureImg(), imageChair);
        }

        MovieDisplayTask imageTask = new MovieDisplayTask(
                mContext,
                mConfig,
                mTaskManager,
                source,
                dimenSpec,
                imageQuality,
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

    private DisplayTaskRecord createTaskRecord(ImageChair settable) {
        long settableId = mSettableIdCreator.createSettableId(settable);
        int taskId = mTaskManager.nextTaskId();
        DisplayTaskRecord displayTaskRecord = new DisplayTaskRecord(settableId, taskId);
        mTaskManager.onDisplayTaskCreated(displayTaskRecord);
        return displayTaskRecord;
    }

    private void showOnLoadingBm(ImageChair<Bitmap> settable, DisplayOption<Bitmap> option) {
        Bitmap showWhenLoading = option.getLoadingImg();
        mImageSettingApplier.applyImageSettings(showWhenLoading, null, settable, null);
    }

    private void showOnLoadingMov(ImageChair<Movie> settable, DisplayOption option) {
        //FIXME
    }

    private DisplayOption<Bitmap> assignBitmapOption(DisplayOption<Bitmap> option) {
        if (option != null) return option;
        return sDefDisplayOption;
    }

    private DisplayOption<Movie> assignMovieOption(DisplayOption<Movie> option) {
        if (option != null) return option;
        return sDefDisplayOptionMovie;
    }

    private boolean onFutureSubmit(BaseFutureTask futureTask) {
        if (futureTask.shouldCancelOthersBeforeRun()) {
            cancel(futureTask.getListenableTask().getTaskRecord().getSettableId());
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
    }

    private ExecutorService getExecutor(ImageSource type) {
        if (type.maybeSlow()) {
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
        return mLoadingService;
    }

    RequestQueueManager<Transaction> getTransactionService() {
        return mTransactionService;
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
    @LoaderApi
    @NonNull
    AbsListViewScrollDetector linkScrollStateTo(@NonNull AbsListView view) {
        mLogger.verbose(view);
        AbsListViewScrollDetector detector = new AbsListViewScrollDetector() {
            @Override
            public void onScrollUp() {
                pause();
            }

            @Override
            public void onScrollDown() {
                pause();
            }

            @Override
            public void onIdle() {
                resume();
            }
        };
        Preconditions.checkNotNull(view).setOnScrollListener(detector);
        detector.setListView(view);
        return detector;
    }

    @LoaderApi
    public void unLinkScrollStateTo(@NonNull AbsListView view) {
        mLogger.verbose(view);
        view.setOnScrollListener(null);
    }

    /**
     * Call this to pause the {@link ImageLoader}
     */
    @LoaderApi
    public void pause() {
        ensureNotTerminated();
        if (!isPaused()) {
            mState = LoaderState.PAUSE_REQUESTED;
            mLogger.funcExit();
        }
    }

    /**
     * @return {@code true} if this loader is paused.
     * @see #pause()
     */
    @LoaderApi
    public boolean isPaused() {
        synchronized (this) {
            return mState == LoaderState.PAUSED || mState == LoaderState.PAUSE_REQUESTED;
        }
    }

    /**
     * @return {@code true} if this loader is terminated.
     * @see #terminate() ()
     */
    @LoaderApi
    public boolean isTerminated() {
        synchronized (this) {
            return mState == LoaderState.TERMINATED;
        }
    }

    /**
     * Call this to resume the {@link ImageLoader} from pause state.
     *
     * @see #pause()
     * @see LoaderState
     */
    @LoaderApi
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
    @LoaderApi
    public void clearTasks() {
        ensureNotTerminated();
        mTaskManager.clearTasks();
    }

    /**
     * Terminate the loader.
     */
    @LoaderApi
    @Override
    public synchronized void terminate() {
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
    @LoaderApi
    public void cancelAllTasks() {
        synchronized (mFutures) {
            for (BaseFutureTask futureTask : mFutures) {
                futureTask.cancel(true);
            }
            mFutures.clear();
        }
    }

    /**
     * Cancel the load and loading task who's from match given.
     *
     * @param url The from of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(@NonNull String url) {
        Preconditions.checkNotNull(url);
        List<BaseFutureTask> pendingCancels = findTasks(url);
        if (pendingCancels.size() > 0) {
            for (BaseFutureTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                mUiThreadRouter.callOnCancel(toBeCanceled.getListenableTask().getProgressListener());
                mLogger.info("Cancel task for from:" + url);
            }
            pendingCancels.clear();
        }
        return this;
    }

    /**
     * Cancel the load and loading task who's view match given.
     *
     * @param view The view of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(@NonNull ImageView view) {
        return cancel(new BitmapImageViewDelegate(view));
    }

    /**
     * Cancel the load and loading task who's settable match given.
     *
     * @param settable The settable of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(@NonNull ImageChair settable) {
        return cancel(mSettableIdCreator.createSettableId(settable));
    }

    /**
     * Cancel the load and loading task who's settableId match given.
     *
     * @param settableId The settableId of the loader request.
     */
    @LoaderApi
    public ImageLoader cancel(long settableId) {
        Preconditions.checkState(settableId != 0, "Invalid settable with id:0");
        List<BaseFutureTask> pendingCancels = findTasks(settableId);
        if (pendingCancels.size() > 0) {
            for (BaseFutureTask toBeCanceled : pendingCancels) {
                toBeCanceled.cancel(true);
                mUiThreadRouter.callOnCancel(toBeCanceled.getListenableTask().getProgressListener());
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
                        && url.equals(futureTask.getListenableTask().getImageData())) {
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
                        && settableId == futureTask.getListenableTask().getTaskRecord().getSettableId()) {
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
            mLogger.debug("Freezing the loader...");
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
    public ImageLoader fork(LoaderConfig param) {
        return clone(this, param);
    }

    @LoaderApi
    public long getInternalStorageUsage() {
        return StorageStats.from(mContext).getInternalStorageUsage();
    }

    @LoaderApi
    public long getExternalStorageUsage() {
        return StorageStats.from(mContext).getExternalStorageUsage();
    }

    @LoaderApi
    public long getTotalStorageUsage() {
        return StorageStats.from(mContext).getTotalStorageUsage();
    }

    @LoaderApi
    public long getTotalTrafficUsage() {
        return TrafficStats.from(mContext).getTotalTrafficUsage();
    }

    @LoaderApi
    public long getMobileTrafficUsage() {
        return TrafficStats.from(mContext).getMobileTrafficUsage();
    }

    @LoaderApi
    public long getWifiTrafficUsage() {
        return TrafficStats.from(mContext).getWifiTrafficUsage();
    }

    private class TaskHandler implements RequestHandler<BaseFutureTask> {
        @Override
        public boolean handleRequest(BaseFutureTask request) {
            freezeIfRequested();
            if (!onFutureSubmit(request)) return false;
            getExecutor(request.getListenableTask().getImageData().getType()).submit(request);
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
