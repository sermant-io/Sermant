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

package com.huawei.gray.dubbo.strategy.rule;

import com.huawei.gray.dubbo.strategy.InvokerChooser;
import com.huawei.gray.dubbo.strategy.InvokerStrategy;
import com.huawei.gray.dubbo.strategy.RuleStrategy;
import com.huawei.gray.dubbo.strategy.VersionChooser;
import com.huawei.gray.dubbo.strategy.VersionStrategy;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.VersionFrom;
import com.huawei.route.common.utils.CollectionUtils;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 流量匹配
 *
 * @author provenceee
 * @since 2021/10/14
 */
public class WeightRuleStrategy implements RuleStrategy {
    private static final int ONO_HUNDRED = 100;

    @Override
    public List<Invoker<?>> getTargetInvoker(List<Route> routes, Invocation invocation, List<Invoker<?>> invokers,
        VersionFrom versionFrom) {
        if (routes.get(0).getWeight() == null) {
            // 规定第一个规则的流量为空，则设置为100
            routes.get(0).setWeight(ONO_HUNDRED);
        }
        String targetVersion = null;
        Set<String> notMatchVersions = new HashSet<String>();
        int begin = 1;
        int num = new Random().nextInt(ONO_HUNDRED) + 1;
        for (Route route : routes) {
            @SuppressWarnings("checkstyle:RegexpSingleline")
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            String currentVersion = route.getTags().getVersion();
            if (num >= begin && num <= begin + weight - 1) {
                targetVersion = currentVersion;
                break;
            }
            begin += weight;
            notMatchVersions.add(currentVersion);
        }
        VersionStrategy versionStrategy = VersionChooser.INSTANCE.choose(versionFrom);
        InvokerStrategy invokerStrategy = InvokerChooser.INSTANCE.choose(targetVersion);
        List<Invoker<?>> resultList = new ArrayList<Invoker<?>>();
        for (Invoker<?> invoker : invokers) {
            if (invokerStrategy.isMatch(invoker, targetVersion, notMatchVersions, versionStrategy)) {
                resultList.add(invoker);
            }
        }
        return CollectionUtils.isEmpty(resultList) ? invokers : resultList;
    }
}