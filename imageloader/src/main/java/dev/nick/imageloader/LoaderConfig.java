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

import dev.nick.imageloader.cache.CachePolicy;

/**
 * Configuration for {@link ImageLoader}, use a {@link Builder}
 * to build one.
 */
public class LoaderConfig {

    private int nLoadingThreads, nCachingThreads;
    private boolean memCacheEnabled, diskCacheEnabled, debug;

    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isDiskCacheEnabled() {
        return diskCacheEnabled;
    }

    public boolean isMemCacheEnabled() {
        return memCacheEnabled;
    }

    public int getCachingThreads() {
        return nCachingThreads;
    }

    public int getLoadingThreads() {
        return nLoadingThreads;
    }

    private CachePolicy cachePolicy;

    private LoaderConfig(CachePolicy cachePolicy, boolean diskCacheEnabled, boolean memCacheEnabled,
                         boolean debug,
                         int nCachingThreads, int nLoadingThreads) {
        this.cachePolicy = cachePolicy;
        this.diskCacheEnabled = diskCacheEnabled;
        this.memCacheEnabled = memCacheEnabled;
        this.debug = debug;
        this.nCachingThreads = nCachingThreads;
        this.nLoadingThreads = nLoadingThreads;
    }

    public static class Builder {

        int nLoadingThreads, nCachingThreads;
        boolean memCacheEnabled, diskCacheEnabled, debug;
        CachePolicy cachePolicy;

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
         * @param diskCacheEnabled {@code true} to enabled disk cache.
         * @return Builder instance.
         */
        public Builder diskCacheEnabled(boolean diskCacheEnabled) {
            this.diskCacheEnabled = diskCacheEnabled;
            return Builder.this;
        }

        /**
         * @param memCacheEnabled {@code true} to enabled memory cache.
         * @return Builder instance.
         */
        public Builder memCacheEnabled(boolean memCacheEnabled) {
            this.memCacheEnabled = memCacheEnabled;
            return Builder.this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return Builder.this;
        }

        /**
         * @param nCachingThreads Number of threads when caching.
         * @return Builder instance.
         */
        public Builder cachingThreads(int nCachingThreads) {
            this.nCachingThreads = nCachingThreads;
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
            return new LoaderConfig(cachePolicy, diskCacheEnabled, memCacheEnabled,
                    debug, nCachingThreads, nLoadingThreads);
        }
    }
}
