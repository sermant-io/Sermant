/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 *
 */

package com.huaweicloud.loadbalancer.utils;

import com.huaweicloud.loadbalancer.rule.ChangedLoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * test the ribbon utility class
 *
 * @author zhouss
 * @since 2022-08-16
 */
public class CacheUtilsTest {
    private static final String OLD_SERVICE_NAME = "oldService";
    private static final String NEW_SERVICE_NAME = "newService";
    private static final String RULE = "Random";

    /**
     * test cache refresh
     */
    @Test
    public void testCache() {
        // judgment type test
        final LoadbalancerRule newRule = new LoadbalancerRule(NEW_SERVICE_NAME, RULE);
        final Map<String, Object> cache = buildCache();
        Assert.assertFalse(CacheUtils.updateCache(cache, newRule));
        // test cleanup data
        final Map<String, Object> changeCache = buildCache();
        final ChangedLoadbalancerRule changedLoadbalancerRule = new ChangedLoadbalancerRule(
                new LoadbalancerRule(null, RULE), new LoadbalancerRule(OLD_SERVICE_NAME, RULE));
        Assert.assertTrue(CacheUtils.updateCache(changeCache, changedLoadbalancerRule));
        Assert.assertTrue(changeCache.isEmpty());
        // There are other service names, not just the old and the new
        final Map<String, Object> cacheMore = buildCache();
        cacheMore.put("otherService", new Object());
        final ChangedLoadbalancerRule moreChangeRule = new ChangedLoadbalancerRule(
                new LoadbalancerRule(NEW_SERVICE_NAME, RULE), new LoadbalancerRule(OLD_SERVICE_NAME, RULE));
        Assert.assertTrue(CacheUtils.updateCache(cacheMore, moreChangeRule));
        Assert.assertEquals(1, cacheMore.size());
    }

    private Map<String, Object> buildCache() {
        final HashMap<String, Object> cache = new HashMap<>();
        final Object placeHolder = new Object();
        cache.put(OLD_SERVICE_NAME, placeHolder);
        cache.put(NEW_SERVICE_NAME, placeHolder);
        return cache;
    }
}
