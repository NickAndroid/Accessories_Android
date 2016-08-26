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

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.guava.base.Optional;

import dev.nick.accessories.common.annotation.AccessoryApi;
import dev.nick.accessories.common.annotation.MinSize;
import dev.nick.accessories.media.cache.CachePolicy;
import dev.nick.accessories.media.queue.QueuePolicy;
import dev.nick.accessories.media.utils.Preconditions;
import dev.nick.accessories.media.worker.network.NetworkPolicy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Configuration for {@link MediaAccessory}, use a {@link Builder}
 * to build one, or using {@link #DEFAULT_CONFIG} as a default config.
 */
@AccessoryApi
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessoryConfig {

    public static final AccessoryConfig DEFAULT_CONFIG = AccessoryConfig.builder()
            .cachePolicy(CachePolicy.DEFAULT_CACHE_POLICY)
            .networkPolicy(NetworkPolicy.DEFAULT_NETWORK_POLICY)
            .queuePolicy(QueuePolicy.FIFO)
            .loadingThreads((Runtime.getRuntime().availableProcessors() + 1) / 2)
            .debugLevel(Log.DEBUG)
            .build();

    private CachePolicy cachePolicy;
    private NetworkPolicy networkPolicy;
    private QueuePolicy queuePolicy;

    @MinSize(1)
    private int loadingThreads;

    @MinSize(0)
    private int debugLevel;

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private Optional<Integer> nLoadingThreads = Optional.absent();

        private Optional<CachePolicy> cachePolicy = Optional.absent();
        private Optional<NetworkPolicy> networkPolicy = Optional.absent();
        private Optional<QueuePolicy> queuePolicy = Optional.absent();

        private Optional<Integer> debugLevel = Optional.absent();

        /**
         * @param cachePolicy The {@link CachePolicy} using to cache.
         * @return Builder instance.
         * @see CachePolicy
         */
        public Builder cachePolicy(@NonNull CachePolicy cachePolicy) {
            this.cachePolicy = Optional.of(cachePolicy);
            return Builder.this;
        }

        /**
         * @param networkPolicy The {@link NetworkPolicy} using to download images.
         * @return Builder instance.
         * @see CachePolicy
         */
        public Builder networkPolicy(@NonNull NetworkPolicy networkPolicy) {
            this.networkPolicy = Optional.of(networkPolicy);
            return Builder.this;
        }

        /**
         * @param queuePolicy The {@link QueuePolicy} using for queue.
         * @return Builder instance.
         * @see QueuePolicy
         * @deprecated Do not call anymore, FIFO is preferred by force.
         */
        public Builder queuePolicy(@NonNull QueuePolicy queuePolicy) {
            this.queuePolicy = Optional.of(queuePolicy);
            return Builder.this;
        }

        /**
         * @param nLoadingThreads Number of threads when loading.
         * @return Builder instance.
         */
        public Builder loadingThreads(@MinSize(1) int nLoadingThreads) {
            Preconditions.checkState(nLoadingThreads > 0, "Loading thread count should be positive");
            this.nLoadingThreads = Optional.of(nLoadingThreads);
            return Builder.this;
        }

        /**
         * @param debugLevel Debug level of Loader.
         * @return Builder instance.
         */
        public Builder debugLevel(@MinSize(0) int debugLevel) {
            Preconditions.checkState(debugLevel > 0, "Debug level should be positive");
            this.debugLevel = Optional.of(debugLevel);
            return Builder.this;
        }

        public AccessoryConfig build() {
            return new AccessoryConfig(
                    cachePolicy.or(CachePolicy.DEFAULT_CACHE_POLICY),
                    networkPolicy.or(NetworkPolicy.DEFAULT_NETWORK_POLICY),
                    queuePolicy.or(QueuePolicy.FIFO),
                    nLoadingThreads.or(Runtime.getRuntime().availableProcessors()),
                    debugLevel.or(Log.DEBUG));
        }
    }
}
