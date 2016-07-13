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

import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.loader.network.NetworkPolicy;
import dev.nick.logger.LoggerManager;

/**
 * Configuration for {@link ImageLoader}, use a {@link Builder}
 * to build one, or using {@link #DEFAULT_CONFIG} as a default config.
 */
public class LoaderConfig {

    public static final LoaderConfig DEFAULT_CONFIG = new Builder()
            .cachePolicy(CachePolicy.DEFAULT_CACHE_POLICY)
            .networkPolicy(NetworkPolicy.DEFAULT_NETWORK_POLICY)
            .loadingThreads(Runtime.getRuntime().availableProcessors())
            .build();

    private int nLoadingThreads;

    private CachePolicy cachePolicy;
    private NetworkPolicy networkPolicy;

    @NonNull
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    public NetworkPolicy getNetworkPolicy() {
        return networkPolicy;
    }

    public int getLoadingThreads() {
        return nLoadingThreads;
    }


    private LoaderConfig(CachePolicy cachePolicy,
                         NetworkPolicy networkPolicy,
                         int nLoadingThreads) {
        this.cachePolicy = cachePolicy;
        this.networkPolicy = networkPolicy;
        this.nLoadingThreads = nLoadingThreads;
    }

    public static class Builder {

        private int nLoadingThreads;
        private CachePolicy cachePolicy;
        private NetworkPolicy networkPolicy;

        public Builder() {
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
         * @param nLoadingThreads Number of threads when loading.
         * @return Builder instance.
         */
        public Builder loadingThreads(int nLoadingThreads) {
            this.nLoadingThreads = nLoadingThreads;
            return Builder.this;
        }

        public LoaderConfig build() {
            invalidate();
            return new LoaderConfig(
                    cachePolicy,
                    networkPolicy,
                    nLoadingThreads);
        }

        void invalidate() {
            if (cachePolicy == null) {
                cachePolicy = CachePolicy.DEFAULT_CACHE_POLICY;
                LoggerManager.getLogger(ImageLoader.class).warn("Using default cache policy:" + cachePolicy);
            }
            if (networkPolicy == null) {
                networkPolicy = NetworkPolicy.DEFAULT_NETWORK_POLICY;
                LoggerManager.getLogger(ImageLoader.class).warn("Using default network policy:" + networkPolicy);
            }
            if (nLoadingThreads <= 0) {
                LoggerManager.getLogger(ImageLoader.class).warn("Using [Runtime.availableProcessors] as nLoadingThreads");
                nLoadingThreads = Runtime.getRuntime().availableProcessors();
            }
        }
    }
}
