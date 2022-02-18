/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.gray.feign.rule;

import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.entity.Route;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 权重标签匹配
 *
 * @author lilai
 * @since 2021-11-03
 */
public class WeightRuleStrategy implements RuleStrategy {
    private static final int ONO_HUNDRED = 100;

    @Override
    public Instances getTargetServiceInstance(List<Route> list, String targetService,
        Map<String, Collection<String>> headers) {
        Map<String, List<Instances>> map = AddrCache.getAddr(targetService, null);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }

        // 剔除不合法的路由规则，和不在地址列表中的版本应用地址
        Iterator<Route> iterator = list.iterator();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            if (route.getTags() == null || !map.containsKey(route.getTags().getVersion())) {
                iterator.remove();
            }
        }
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        if (list.get(0).getWeight() == null) {
            // 规定第一个规则不为空，则设置为100
            list.get(0).setWeight(ONO_HUNDRED);
        }
        int begin = 1;
        int num = new Random().nextInt(ONO_HUNDRED) + 1;
        for (Route route : list) {
            @SuppressWarnings("checkstyle:RegexpSingleline")
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            String tagVersion = route.getTags().getVersion();
            if (num >= begin && num <= begin + weight - 1) {
                return map.get(tagVersion).get(0);
            }
            begin += weight;
        }
        return null;
    }
}