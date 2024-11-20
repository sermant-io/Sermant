/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/rpc/cluster/loadbalance/ShortestResponseLoadBalance.java
 * from the Apache Dubbo project.
 */

package io.sermant.registry.grace.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.registry.config.ConfigConstants;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.support.RegisterSwitchSupport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Elegant on-line and off-line switches
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class GraceSwitchInterceptor extends RegisterSwitchSupport {
    /**
     * grace configuration class
     */
    protected final GraceConfig graceConfig;

    private Random random = new Random();

    /**
     * Elegant on-line and off-line switches
     *
     * @since 2022-05-17
     */
    public GraceSwitchInterceptor() {
        graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    @Override
    protected boolean isEnabled() {
        return graceConfig.isEnableSpring();
    }

    /**
     * Build endpoints
     *
     * @param host domain name
     * @param port Port
     * @return endpoint
     */
    protected String buildEndpoint(String host, int port) {
        return host + ":" + port;
    }

    /**
     * Warm-up information
     *
     * @param ip IP address of the instance
     * @param port Instance port
     */
    protected void warmMessage(String ip, int port) {
        LoggerFactory.getLogger().fine(String.format(Locale.ENGLISH, "Instance [%s:%s] is warming up!", ip, port));
    }

    /**
     * Weights are calculated for individual instances
     *
     * @param metadata Original information
     * @param weights Weight allocation
     * @param index Index of the current instance
     * @return Check whether the preheating of the current subscript instance is complete
     */
    protected boolean calculate(Map<String, String> metadata, int[] weights, int index) {
        final String warmUpWeightStr = metadata.getOrDefault(GraceConstants.WARM_KEY_WEIGHT,
                String.valueOf(GraceConstants.DEFAULT_WARM_UP_WEIGHT));
        final String warmUpTimeStr = metadata
                .getOrDefault(GraceConstants.WARM_KEY_TIME, GraceConstants.DEFAULT_WARM_UP_TIME);
        final String warmUpCurveStr = metadata
                .getOrDefault(GraceConstants.WARM_KEY_CURVE, String.valueOf(GraceConstants.DEFAULT_WARM_UP_CURVE));
        String injectTimeStr = metadata.getOrDefault(GraceConstants.WARM_KEY_INJECT_TIME,
                GraceConstants.DEFAULT_WARM_UP_INJECT_TIME_GAP);
        final long injectTime = Long.parseLong(injectTimeStr);
        final long warmUpTime = Integer.parseInt(warmUpTimeStr) * ConfigConstants.SEC_DELTA;
        final int weight = this.calculateWeight(injectTime, warmUpTime, warmUpWeightStr, warmUpCurveStr);
        weights[index] = weight;
        return isWarmed(injectTime, warmUpTime);
    }

    /**
     * Whether the warm-up is complete
     *
     * @param injectTime If the injection time is 0, the preheating function is not enabled for the current instance and
     * the maximum weight is returned
     *
     * @param warmUpTime Warm-up time
     * @return Whether the warm-up is complete
     */
    private boolean isWarmed(long injectTime, long warmUpTime) {
        return injectTime == 0L || System.currentTimeMillis() - injectTime > warmUpTime;
    }

    /**
     * Calculate the weights
     *
     * @param injectTime Warm-up parameter injection time
     * @param warmUpTime Warm-up time
     * @param warmUpWeightStr Warm-up weights
     * @param warmUpCurveStr Preheat calculates the curve value
     * @return Weight
     */
    protected int calculateWeight(long injectTime, long warmUpTime, String warmUpWeightStr,
            String warmUpCurveStr) {
        final int warmUpWeight = Integer.parseInt(warmUpWeightStr);
        int warmUpCurve = Integer.parseInt(warmUpCurveStr);
        if (warmUpTime <= 0 || injectTime <= 0) {
            // The default weight of services that do not enable prefetch is 100
            return warmUpWeight;
        }
        if (warmUpCurve < 0) {
            warmUpCurve = GraceConstants.DEFAULT_WARM_UP_CURVE;
        }
        final long runtime = System.currentTimeMillis() - injectTime;
        if (runtime > 0 && runtime < warmUpTime) {
            // The warm-up is not over
            return calculateWeight(runtime, warmUpTime, warmUpCurve, warmUpWeight);
        }
        return Math.max(0, warmUpWeight);
    }

    /**
     * Calculate the weights
     *
     * @param runtime Runtime (from startup)
     * @param warmUpTime Warm-up time
     * @param warmUpCurve Preheating a calculation curve
     * @param warmUpWeight Warm-up weights
     * @return Weight
     */
    protected int calculateWeight(double runtime, double warmUpTime, int warmUpCurve, int warmUpWeight) {
        final int round = (int) Math.round(Math.pow(runtime / warmUpTime, warmUpCurve) * warmUpWeight);
        return round < 1 ? 1 : Math.min(round, warmUpWeight);
    }

    /**
     * Select an instance
     *
     * @param totalWeight Total weight
     * @param weights Weight allocation based on all instances
     * @param serverList List of service instances
     * @return Identify the instance
     */
    protected Optional<Object> chooseServer(int totalWeight, int[] weights, List<?> serverList) {
        if (totalWeight <= 0) {
            return Optional.empty();
        }
        int position = random.nextInt(totalWeight);
        for (int i = 0; i < weights.length; i++) {
            position -= weights[i];
            if (position < 0) {
                return Optional.of(serverList.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Obtain the request header of the local IP address
     *
     * @return Request header
     */
    protected Map<String, List<String>> getGraceIpHeaders() {
        String address = RegisterContext.INSTANCE.getClientInfo().getIp() + ":" + graceConfig.getHttpServerPort();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(GraceConstants.SERMANT_GRACE_ADDRESS, Collections.singletonList(address));
        headers.put(GraceConstants.GRACE_OFFLINE_SOURCE_KEY,
                Collections.singletonList(GraceConstants.GRACE_OFFLINE_SOURCE_VALUE));
        return headers;
    }
}
