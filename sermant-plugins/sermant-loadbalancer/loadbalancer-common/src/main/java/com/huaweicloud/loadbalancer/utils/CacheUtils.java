/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.Map;
import java.util.Objects;

/**
 * cache utility class
 *
 * @author zhouss
 * @since 2022-08-12
 */
public class CacheUtils {
    private CacheUtils() {
    }

    /**
     * update the cache only for modify events
     *
     * @param cache cache
     * @param rule updated rule
     * @return whether to update
     */
    public static boolean updateCache(Map<String, ?> cache, LoadbalancerRule rule) {
        if (!(rule instanceof ChangedLoadbalancerRule)) {
            return false;
        }
        ChangedLoadbalancerRule changedLoadbalancerRule = (ChangedLoadbalancerRule) rule;
        final String oldServiceName = changedLoadbalancerRule.getOldRule().getServiceName();
        final String newServiceName = changedLoadbalancerRule.getNewRule().getServiceName();
        if (Objects.isNull(oldServiceName) || Objects.isNull(newServiceName)) {
            cache.clear();
            return true;
        }
        cache.remove(oldServiceName);
        cache.remove(newServiceName);
        return true;
    }
}
