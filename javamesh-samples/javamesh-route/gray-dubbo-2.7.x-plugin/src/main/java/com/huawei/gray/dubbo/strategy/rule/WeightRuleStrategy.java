/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.rule;

import com.huawei.gray.dubbo.strategy.RuleStrategy;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.entity.Route;

import org.apache.dubbo.rpc.Invocation;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 流量匹配
 *
 * @author pengyuyi
 * @date 2021/10/14
 */
public class WeightRuleStrategy implements RuleStrategy {
    @Override
    public String getTargetServiceIp(List<Route> list, String targetService, String interfaceName, String version,
            Invocation invocation) {
        Map<String, List<Instances>> map = AddrCache.getAddr(targetService);
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
            // 规定第一个规则的流量为空，则设置为100
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
                Instances instance = map.get(tagVersion).get(0);
                return RouterUtil.getTargetAndSetAttachment(instance, invocation, tagVersion, instance.getLdc());
            }
            begin += weight;
        }
        return null;
    }
}