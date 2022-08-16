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
 * ribbon负载均衡工具类
 *
 * @author zhouss
 * @since 2022-08-12
 */
public class RibbonUtils {
    /**
     * 默认的ribbon负载均衡键
     */
    public static final String DEFAULT_RIBBON_LOADBALANCER_KEY = "default";

    private RibbonUtils() {
    }

    /**
     * 更新缓存, 只针对modify事件
     *
     * @param cache 缓存
     * @param rule 更新的规则
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
