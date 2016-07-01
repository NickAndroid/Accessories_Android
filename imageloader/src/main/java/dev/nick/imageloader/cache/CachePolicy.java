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

/**
 * Policy reading when caching images.
 * Using {@link Builder} to build a policy.
 */
public class CachePolicy {

    public static final FileNameGenerator DEFAULT_FILENAME_GENERATOR = new HeadlessFileNameGenerator();
    public static final KeyGenerator DEFAULT_KEY_GENERATOR = new HashcodeKeyGenerator();
    public static final CachePolicy DEFAULT_CACHE_POLICY = new CachePolicy.Builder()
            .compressFormat(Bitmap.CompressFormat.PNG)
            .quality(Quality.BEST)
            .fileNameGenerator(CachePolicy.DEFAULT_FILENAME_GENERATOR)
            .keyGenerator(CachePolicy.DEFAULT_KEY_GENERATOR)
            .preferredLocation(CachePolicy.Location.EXTERNAL)
            .build();

    private CachePolicy(FileNameGenerator fileNameGenerator, KeyGenerator keyGenerator,
                        Bitmap.CompressFormat compressFormat, int quality,
                        int preferredLocation) {
        this.fileNameGenerator = fileNameGenerator;
        this.keyGenerator = keyGenerator;
        this.preferredLocation = preferredLocation;
        this.compressFormat = compressFormat;
        this.quality = quality;
    }

    private int preferredLocation;
    private KeyGenerator keyGenerator;
    private FileNameGenerator fileNameGenerator;
    private Bitmap.CompressFormat compressFormat;
    private int quality;

    public int getPreferredLocation() {
        return preferredLocation;
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

    public static class Builder {

        private int preferredLocation;
        private KeyGenerator keyGenerator;
        private FileNameGenerator fileNameGenerator;
        private Bitmap.CompressFormat compressFormat;
        private int quality;

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

        public Builder quality(int quality) {
            this.quality = quality;
            return Builder.this;
        }

        public CachePolicy build() {
            invalidate();
            return new CachePolicy(fileNameGenerator, keyGenerator, compressFormat, quality, preferredLocation);
        }

        void invalidate() {
            if (keyGenerator == null) keyGenerator(DEFAULT_KEY_GENERATOR);
            if (fileNameGenerator == null) fileNameGenerator(DEFAULT_FILENAME_GENERATOR);
            if (compressFormat == null) compressFormat(Bitmap.CompressFormat.PNG);
            if (quality == 0) quality(Quality.BEST);
            if (preferredLocation == 0) preferredLocation(Location.EXTERNAL);
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
