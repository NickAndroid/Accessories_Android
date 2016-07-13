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

package dev.nick.imageloader.cache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.nick.imageloader.ImageLoader;
import dev.nick.logger.LoggerManager;

/**
 * Policy reading when caching images.
 * Using {@link Builder} to build a policy.
 */
public class CachePolicy {

    public static final int DEFAULT_MEM_CACHE_POOL_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);
    public static final FileNameGenerator DEFAULT_FILENAME_GENERATOR = new HeadlessFileNameGenerator();
    public static final KeyGenerator DEFAULT_KEY_GENERATOR = new HashcodeKeyGenerator();

    public static final CachePolicy DEFAULT_CACHE_POLICY = new CachePolicy.Builder()
            .enableDiskCache()
            .enableMemCache()
            .cachingThreads(Runtime.getRuntime().availableProcessors())
            .memCachePoolSize(DEFAULT_MEM_CACHE_POOL_SIZE)
            .compressFormat(Bitmap.CompressFormat.PNG)
            .imageQuality(Quality.BEST)
            .fileNameGenerator(CachePolicy.DEFAULT_FILENAME_GENERATOR)
            .keyGenerator(CachePolicy.DEFAULT_KEY_GENERATOR)
            .preferredLocation(CachePolicy.Location.EXTERNAL)
            .build();

    private CachePolicy(boolean memCacheEnabled,
                        boolean diskCacheEnabled,
                        int nCachingThreads,
                        int memCachePoolSize,
                        FileNameGenerator fileNameGenerator,
                        KeyGenerator keyGenerator,
                        Bitmap.CompressFormat compressFormat,
                        int quality,
                        int preferredLocation) {
        this.memCacheEnabled = memCacheEnabled;
        this.diskCacheEnabled = diskCacheEnabled;
        this.nCachingThreads = nCachingThreads;
        this.memCachePoolSize = memCachePoolSize;
        this.fileNameGenerator = fileNameGenerator;
        this.keyGenerator = keyGenerator;
        this.preferredLocation = preferredLocation;
        this.compressFormat = compressFormat;
        this.quality = quality;
    }

    private boolean memCacheEnabled;
    private boolean diskCacheEnabled;
    private int nCachingThreads;
    private int memCachePoolSize;
    private int preferredLocation;
    private KeyGenerator keyGenerator;
    private FileNameGenerator fileNameGenerator;
    private Bitmap.CompressFormat compressFormat;
    private int quality;

    public boolean isDiskCacheEnabled() {
        return diskCacheEnabled;
    }

    public boolean isMemCacheEnabled() {
        return memCacheEnabled;
    }

    public int getMemCachePoolSize() {
        return memCachePoolSize;
    }

    public int getPreferredLocation() {
        return preferredLocation;
    }

    public int getCachingThreads() {
        return nCachingThreads;
    }

    @NonNull
    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    @NonNull
    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    @NonNull
    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public int getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return "CachePolicy{" +
                "compressFormat=" + compressFormat +
                ", preferredLocation=" + preferredLocation +
                ", keyGenerator=" + keyGenerator +
                ", fileNameGenerator=" + fileNameGenerator +
                ", imageQuality=" + quality +
                '}';
    }

    public static class Builder {

        private boolean memCacheEnabled;
        private boolean diskCacheEnabled;
        private int nCachingThreads;
        private int memCachePoolSize;
        private int preferredLocation;
        private KeyGenerator keyGenerator;
        private FileNameGenerator fileNameGenerator;
        private Bitmap.CompressFormat compressFormat;
        private int imageQuality;

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
         * @param nCachingThreads Number of threads when caching.
         * @return Builder instance.
         */
        public Builder cachingThreads(int nCachingThreads) {
            this.nCachingThreads = nCachingThreads;
            return Builder.this;
        }

        /**
         * @param memCachePoolSize Pool size of the mem cache, default is {@link #DEFAULT_MEM_CACHE_POOL_SIZE}
         * @return Builder instance.
         */
        public Builder memCachePoolSize(int memCachePoolSize) {
            this.memCachePoolSize = memCachePoolSize;
            return Builder.this;
        }

        /**
         * @param preferredLocation Preferred cache file location.
         * @return Builder instance.
         * @see Location
         */
        public Builder preferredLocation(int preferredLocation) {
            this.preferredLocation = preferredLocation;
            return Builder.this;
        }

        /**
         * @param keyGenerator {@link KeyGenerator}
         * @return Builder instance.
         */
        public Builder keyGenerator(KeyGenerator keyGenerator) {
            this.keyGenerator = keyGenerator;
            return Builder.this;
        }

        /**
         * @param fileNameGenerator {@link FileNameGenerator}
         * @return Builder instance.
         */
        public Builder fileNameGenerator(FileNameGenerator fileNameGenerator) {
            this.fileNameGenerator = fileNameGenerator;
            return Builder.this;
        }

        public Builder compressFormat(Bitmap.CompressFormat format) {
            this.compressFormat = format;
            return Builder.this;
        }

        public Builder imageQuality(int quality) {
            this.imageQuality = quality;
            return Builder.this;
        }

        public CachePolicy build() {
            invalidate();
            return new CachePolicy(
                    memCacheEnabled,
                    diskCacheEnabled,
                    nCachingThreads,
                    memCachePoolSize,
                    fileNameGenerator,
                    keyGenerator,
                    compressFormat,
                    imageQuality,
                    preferredLocation);
        }

        void invalidate() {
            if (nCachingThreads <= 0) {
                LoggerManager.getLogger(ImageLoader.class).warn("Using [Runtime.availableProcessors] as nCachingThreads");
                nCachingThreads = Runtime.getRuntime().availableProcessors();
            }
            if (memCachePoolSize == 0) {
                memCachePoolSize(DEFAULT_MEM_CACHE_POOL_SIZE);
            } else if (memCachePoolSize >= Runtime.getRuntime().maxMemory()) {
                memCachePoolSize(DEFAULT_MEM_CACHE_POOL_SIZE);
                LoggerManager.getLogger(CachePolicy.class).error("You set too large mem pool size, " +
                        "using default size:" + DEFAULT_MEM_CACHE_POOL_SIZE);
            }
            if (keyGenerator == null) keyGenerator(DEFAULT_KEY_GENERATOR);
            if (fileNameGenerator == null) fileNameGenerator(DEFAULT_FILENAME_GENERATOR);
            if (compressFormat == null) compressFormat(Bitmap.CompressFormat.PNG);
            if (imageQuality <= 0 || imageQuality > Quality.BEST) imageQuality(Quality.BEST);
            if (preferredLocation != Location.EXTERNAL && preferredLocation != Location.INTERNAL) {
                preferredLocation(Location.EXTERNAL);
                LoggerManager.getLogger(CachePolicy.class).error("You set a invalid location, " +
                        "using default: Location.EXTERNAL");
            }
        }
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
}
