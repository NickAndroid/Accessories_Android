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

package dev.nick.accessories.media.cache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.guava.base.Optional;

import dev.nick.accessories.media.annotation.MaxSize;
import dev.nick.accessories.media.annotation.MinSize;
import dev.nick.accessories.media.utils.Preconditions;
import lombok.Getter;
import lombok.ToString;

/**
 * Policy reading when caching images.
 * Using {@link Builder} to build a policy.
 */
@ToString
@Getter
public class CachePolicy {

    public static final int DEFAULT_MEM_CACHE_POOL_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);
    public static final int DEFAULT_CACHING_THREADS = (Runtime.getRuntime().availableProcessors() + 1) / 2;

    public static final FileNameGenerator DEFAULT_FILENAME_GENERATOR = new HeadlessFileNameGenerator();
    public static final KeyGenerator DEFAULT_KEY_GENERATOR = new HashcodeKeyGenerator();

    public static final CachePolicy DEFAULT_CACHE_POLICY = CachePolicy.builder()
            .enableDiskCache()
            .enableMemCache()
            .cachingThreads(DEFAULT_CACHING_THREADS)
            .memCachePoolSize(DEFAULT_MEM_CACHE_POOL_SIZE)
            .compressFormat(Bitmap.CompressFormat.PNG)
            .imageQuality(Quality.BEST)
            .cacheDirName("disk.cache")
            .fileNameGenerator(CachePolicy.DEFAULT_FILENAME_GENERATOR)
            .keyGenerator(CachePolicy.DEFAULT_KEY_GENERATOR)
            .preferredLocation(CachePolicy.Location.EXTERNAL)
            .build();

    private boolean memCacheEnabled;
    private boolean diskCacheEnabled;
    private boolean storageStatsEnabled;

    @MinSize(1)
    private int cachingThreads;
    @MinSize(1)
    private int memCachePoolSize;
    private int preferredLocation;
    private String cacheDirName;
    private KeyGenerator keyGenerator;
    private FileNameGenerator fileNameGenerator;
    private Bitmap.CompressFormat compressFormat;

    @MaxSize(100)
    private int quality;

    private CachePolicy(boolean memCacheEnabled,
                        boolean diskCacheEnabled,
                        boolean storageStatsEnabled,
                        int cachingThreads,
                        int memCachePoolSize,
                        String cacheDirName,
                        FileNameGenerator fileNameGenerator,
                        KeyGenerator keyGenerator,
                        Bitmap.CompressFormat compressFormat,
                        int quality,
                        int preferredLocation) {
        this.memCacheEnabled = memCacheEnabled;
        this.diskCacheEnabled = diskCacheEnabled;
        this.storageStatsEnabled = storageStatsEnabled;
        this.cachingThreads = cachingThreads;
        this.memCachePoolSize = memCachePoolSize;
        this.cacheDirName = cacheDirName;
        this.fileNameGenerator = fileNameGenerator;
        this.keyGenerator = keyGenerator;
        this.preferredLocation = preferredLocation;
        this.compressFormat = compressFormat;
        this.quality = quality;
    }

    public static Builder builder() {
        return new Builder();
    }

    public interface Location {
        int INTERNAL = 0x100;
        int EXTERNAL = 0x101;
    }

    public interface Quality {
        int BEST = 100;
        int HIGH = 60;
        int LOW = 30;
    }

    public static class Builder {

        private boolean memCacheEnabled;
        private boolean diskCacheEnabled;
        private boolean storageStats;

        private Optional<Integer> nCachingThreads = Optional.absent();
        private Optional<Integer> memCachePoolSize = Optional.absent();
        private Optional<Integer> preferredLocation = Optional.absent();

        private Optional<String> cacheDirName = Optional.absent();
        private Optional<KeyGenerator> keyGenerator = Optional.absent();
        private Optional<FileNameGenerator> fileNameGenerator = Optional.absent();
        private Optional<Bitmap.CompressFormat> compressFormat = Optional.absent();

        private Optional<Integer> imageQuality = Optional.absent();

        private Builder() {
        }

        /**
         * To enable disk cache.
         *
         * @return Builder instance.
         */
        public Builder enableDiskCache() {
            this.diskCacheEnabled = true;
            return Builder.this;
        }

        /**
         * To enabled memory cache.
         *
         * @return Builder instance.
         */
        public Builder enableMemCache() {
            this.memCacheEnabled = true;
            return Builder.this;
        }

        /**
         * To enabled storage usage stats.
         *
         * @return Builder instance.
         */
        public Builder enableStorageStats() {
            this.storageStats = true;
            return Builder.this;
        }

        /**
         * @param nCachingThreads Number of threads when caching.
         * @return Builder instance.
         */
        public Builder cachingThreads(@MinSize(1) int nCachingThreads) {
            Preconditions.checkState(nCachingThreads > 1);
            this.nCachingThreads = Optional.of(nCachingThreads);
            return Builder.this;
        }

        /**
         * @param memCachePoolSize Pool size of the mem cache, default is {@link #DEFAULT_MEM_CACHE_POOL_SIZE}
         * @return Builder instance.
         */
        public Builder memCachePoolSize(@MinSize(1024) int memCachePoolSize) {
            Preconditions.checkState(memCachePoolSize >= 1024, "Too small");
            this.memCachePoolSize = Optional.of(memCachePoolSize);
            return Builder.this;
        }

        /**
         * @param preferredLocation Preferred cache file location.
         * @return Builder instance.
         * @see Location
         */
        public Builder preferredLocation(int preferredLocation) {
            Preconditions.checkState(preferredLocation == Location.EXTERNAL
                    || preferredLocation == Location.INTERNAL);
            this.preferredLocation = Optional.of(preferredLocation);
            return Builder.this;
        }

        public Builder cacheDirName(@NonNull String cacheDirName) {
            this.cacheDirName = Optional.of(cacheDirName);
            return Builder.this;
        }

        /**
         * @param keyGenerator {@link KeyGenerator}
         * @return Builder instance.
         */
        public Builder keyGenerator(@NonNull KeyGenerator keyGenerator) {
            this.keyGenerator = Optional.of(keyGenerator);
            return Builder.this;
        }

        /**
         * @param fileNameGenerator {@link FileNameGenerator}
         * @return Builder instance.
         */
        public Builder fileNameGenerator(@NonNull FileNameGenerator fileNameGenerator) {
            this.fileNameGenerator = Optional.of(fileNameGenerator);
            return Builder.this;
        }

        public Builder compressFormat(@NonNull Bitmap.CompressFormat format) {
            this.compressFormat = Optional.of(format);
            return Builder.this;
        }

        public Builder imageQuality(@MaxSize(Quality.BEST) int quality) {
            Preconditions.checkState(quality <= 100);
            this.imageQuality = Optional.of(quality);
            return Builder.this;
        }

        public CachePolicy build() {
            return new CachePolicy(
                    memCacheEnabled,
                    diskCacheEnabled,
                    storageStats,
                    nCachingThreads.or(DEFAULT_CACHING_THREADS),
                    memCachePoolSize.or(DEFAULT_MEM_CACHE_POOL_SIZE),
                    cacheDirName.or("media"),
                    fileNameGenerator.or(DEFAULT_FILENAME_GENERATOR),
                    keyGenerator.or(DEFAULT_KEY_GENERATOR),
                    compressFormat.or(Bitmap.CompressFormat.PNG),
                    imageQuality.or(100),
                    preferredLocation.or(Location.EXTERNAL));
        }
    }
}
