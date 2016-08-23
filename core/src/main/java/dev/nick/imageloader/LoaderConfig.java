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

import android.support.annotation.NonNull;
import android.util.Log;

import dev.nick.imageloader.annotation.LoaderApi;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.queue.QueuePolicy;
import dev.nick.imageloader.worker.network.NetworkPolicy;
import dev.nick.logger.LoggerManager;

/**
 * Configuration for {@link MediaLoader}, use a {@link Builder}
 * to build one, or using {@link #DEFAULT_CONFIG} as a default config.
 */
@LoaderApi
public class LoaderConfig {

    public static final LoaderConfig DEFAULT_CONFIG = LoaderConfig.builder()
            .cachePolicy(CachePolicy.DEFAULT_CACHE_POLICY)
            .networkPolicy(NetworkPolicy.DEFAULT_NETWORK_POLICY)
            .queuePolicy(QueuePolicy.FIFO)
            .loadingThreads((Runtime.getRuntime().availableProcessors() + 1) / 2)
            .debugLevel(Log.WARN)
            .build();

    private int nLoadingThreads;

    private CachePolicy cachePolicy;
    private NetworkPolicy networkPolicy;
    private QueuePolicy queuePolicy;

    private int debugLevel;

    private LoaderConfig(CachePolicy cachePolicy,
                         NetworkPolicy networkPolicy,
                         QueuePolicy queuePolicy,
                         int nLoadingThreads,
                         int debugLevel) {
        this.cachePolicy = cachePolicy;
        this.networkPolicy = networkPolicy;
        this.queuePolicy = queuePolicy;
        this.nLoadingThreads = nLoadingThreads;
        this.debugLevel = debugLevel;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "LoaderConfig{" +
                "nLoadingThreads=" + nLoadingThreads +
                ", cachePolicy=" + cachePolicy +
                ", networkPolicy=" + networkPolicy +
                ", queuePolicy=" + queuePolicy +
                ", debugLevel=" + debugLevel +
                '}';
    }

    @NonNull
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    public NetworkPolicy getNetworkPolicy() {
        return networkPolicy;
    }

    public QueuePolicy getQueuePolicy() {
        return queuePolicy;
    }

    public int getLoadingThreads() {
        return nLoadingThreads;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public static class Builder {

        private int nLoadingThreads;
        private CachePolicy cachePolicy;
        private NetworkPolicy networkPolicy;
        private QueuePolicy queuePolicy;
        private int debugLevel;

        private Builder() {
        }

        /**
         * @param cachePolicy The {@link CachePolicy} using to cache.
         * @return Builder instance.
         * @see CachePolicy
         */
        public Builder cachePolicy(CachePolicy cachePolicy) {
            this.cachePolicy = cachePolicy;
            return Builder.this;
        }

        /**
         * @param networkPolicy The {@link NetworkPolicy} using to download images.
         * @return Builder instance.
         * @see CachePolicy
         */
        public Builder networkPolicy(NetworkPolicy networkPolicy) {
            this.networkPolicy = networkPolicy;
            return Builder.this;
        }

        /**
         * @param queuePolicy The {@link QueuePolicy} using for queue.
         * @return Builder instance.
         * @see QueuePolicy
         * @deprecated Do not call anymore, FIFO is preferred by force.
         */
        public Builder queuePolicy(QueuePolicy queuePolicy) {
            return Builder.this;
        }

        /**
         * @param nLoadingThreads Number of threads when loading.
         * @return Builder instance.
         */
        public Builder loadingThreads(int nLoadingThreads) {
            this.nLoadingThreads = nLoadingThreads;
            return Builder.this;
        }

        /**
         * @param debugLevel Debug level of Loader.
         * @return Builder instance.
         */
        public Builder debugLevel(int debugLevel) {
            this.debugLevel = debugLevel;
            return Builder.this;
        }

        public LoaderConfig build() {
            invalidate();
            return new LoaderConfig(
                    cachePolicy,
                    networkPolicy,
                    queuePolicy,
                    nLoadingThreads,
                    debugLevel);
        }

        void invalidate() {
            if (cachePolicy == null) {
                cachePolicy = CachePolicy.DEFAULT_CACHE_POLICY;
                LoggerManager.getLogger(MediaLoader.class).warn("Using default cache policy:" + cachePolicy);
            }
            if (networkPolicy == null) {
                networkPolicy = NetworkPolicy.DEFAULT_NETWORK_POLICY;
                LoggerManager.getLogger(MediaLoader.class).warn("Using default network policy:" + networkPolicy);
            }
            if (nLoadingThreads <= 0) {
                LoggerManager.getLogger(MediaLoader.class).warn("Using [Runtime.availableProcessors] as nLoadingThreads");
                nLoadingThreads = Runtime.getRuntime().availableProcessors();
            }
            if (queuePolicy == null) {
                LoggerManager.getLogger(MediaLoader.class).warn("Using FIFO as queuePolicy");
                queuePolicy = QueuePolicy.FIFO;
            }
            if (debugLevel < Log.VERBOSE) {
                debugLevel = Log.VERBOSE;
                LoggerManager.getLogger(MediaLoader.class).warn("Using debug level:" + debugLevel);
            }
        }
    }
}
