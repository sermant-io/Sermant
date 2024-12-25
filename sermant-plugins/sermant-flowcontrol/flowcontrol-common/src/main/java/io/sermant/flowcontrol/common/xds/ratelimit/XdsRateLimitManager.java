/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.flowcontrol.common.xds.ratelimit;

import io.sermant.core.service.xds.entity.XdsTokenBucket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Circuit Breaker manager
 *
 * @author zhp
 * @since 2024-12-02
 */
public class XdsRateLimitManager {
    /**
     * The map that stores rate limiting information, where the Key of the first level is the service name
     * and the Key of the second level is the route name
     */
    private static final Map<String, Map<String, XdsRateLimitInfo>> RATE_LIMIT_INFO_MAP = new ConcurrentHashMap<>();

    private XdsRateLimitManager() {
    }

    /**
     * fill and Consumer token
     *
     * @param serviceName service name
     * @param routeName route name
     * @param tokenBucket token rule info
     * @return the result of Consumer results
     */
    public static synchronized boolean fillAndConsumeToken(String serviceName, String routeName,
            XdsTokenBucket tokenBucket) {
        Map<String, XdsRateLimitInfo> limitInfoMap = RATE_LIMIT_INFO_MAP.computeIfAbsent(serviceName,
                key -> new ConcurrentHashMap<>());
        XdsRateLimitInfo xdsRateLimitInfo = limitInfoMap.computeIfAbsent(routeName, key ->
                new XdsRateLimitInfo(System.currentTimeMillis(), tokenBucket.getMaxTokens()));
        fillToken(xdsRateLimitInfo, tokenBucket);
        AtomicInteger currentTokens = xdsRateLimitInfo.getCurrentTokens();
        if (1 <= currentTokens.get()) {
            currentTokens.addAndGet(-1);
            return true;
        }
        return false;
    }

    /**
     * Fill tokenBucket, the token will be consumption when receiving the requests
     *
     * @param xdsRateLimitInfo current rate limit info
     * @param tokenBucket token rule information
     */
    private static void fillToken(XdsRateLimitInfo xdsRateLimitInfo, XdsTokenBucket tokenBucket) {
        long now = System.currentTimeMillis();
        long timeElapsed = now - xdsRateLimitInfo.getLastFilledTime();
        long tokensToAdd = (timeElapsed / tokenBucket.getFillInterval()) * tokenBucket.getTokensPerFill();
        if (tokensToAdd > 0) {
            int currentToken = xdsRateLimitInfo.getCurrentTokens().get();
            xdsRateLimitInfo.getCurrentTokens().set((int) Math.min(tokenBucket.getMaxTokens(),
                    currentToken + tokensToAdd));
            xdsRateLimitInfo.setLastFilledTime(now);
        }
    }
}
