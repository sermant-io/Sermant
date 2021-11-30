/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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
            list.get(0).setWeight(100);
        }
        int begin = 1;
        int num = new Random().nextInt(100) + 1;
        for (Route route : list) {
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