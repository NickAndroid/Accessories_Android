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

/**
 * Policy reading when caching images.
 * Using {@link Builder} to build a policy.
 */
public class CachePolicy {

    public static final FileNameGenerator DEFAULT_FILENAME_GENERATOR = new HeadlessFileNameGenerator();
    public static final KeyGenerator DEFAULT_KEY_GENERATOR = new HashcodeKeyGenerator();

    private CachePolicy(FileNameGenerator fileNameGenerator, KeyGenerator keyGenerator, int preferredLocation) {
        this.fileNameGenerator = fileNameGenerator;
        this.keyGenerator = keyGenerator;
        this.preferredLocation = preferredLocation;
    }

    private int preferredLocation;
    private KeyGenerator keyGenerator;
    private FileNameGenerator fileNameGenerator;

    public int getPreferredLocation() {
        return preferredLocation;
    }

    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public static class Builder {

        int preferredLocation;
        private KeyGenerator keyGenerator;
        private FileNameGenerator fileNameGenerator;

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

        public CachePolicy build() {
            return new CachePolicy((fileNameGenerator == null
                    ? DEFAULT_FILENAME_GENERATOR
                    : fileNameGenerator),
                    (keyGenerator == null
                            ? DEFAULT_KEY_GENERATOR
                            : keyGenerator),
                    preferredLocation);
        }
    }

    public interface Location {
        int INTERNAL = 0;
        int EXTERNAL = 1;
    }
}
