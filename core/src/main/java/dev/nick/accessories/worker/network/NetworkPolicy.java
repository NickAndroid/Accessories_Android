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

package dev.nick.accessories.worker.network;

public class NetworkPolicy {

    public static final NetworkPolicy DEFAULT_NETWORK_POLICY = NetworkPolicy.builder().build();

    boolean onlyOnWifi;
    boolean trafficStatsEnabled;

    private NetworkPolicy(boolean onlyOnWifi, boolean trafficStatsEnabled) {
        this.onlyOnWifi = onlyOnWifi;
        this.trafficStatsEnabled = trafficStatsEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isOnlyOnWifi() {
        return onlyOnWifi;
    }

    public boolean isTrafficStatsEnabled() {
        return trafficStatsEnabled;
    }

    @Override
    public String toString() {
        return "NetworkPolicy{" +
                "onlyOnWifi=" + onlyOnWifi +
                '}';
    }

    public static class Builder {

        boolean onlyOnWifi;
        boolean trafficStatsEnbaled;

        private Builder() {
        }

        /**
         * To load image only under WIFI connection.
         *
         * @return Builder instance.
         */
        public Builder onlyOnWifi() {
            this.onlyOnWifi = true;
            return Builder.this;
        }

        /**
         * To enable the traffic stats.
         *
         * @return Builder instance.
         */
        public Builder enableTrafficStats() {
            this.trafficStatsEnbaled = true;
            return Builder.this;
        }

        public NetworkPolicy build() {
            return new NetworkPolicy(onlyOnWifi, trafficStatsEnbaled);
        }
    }
}
