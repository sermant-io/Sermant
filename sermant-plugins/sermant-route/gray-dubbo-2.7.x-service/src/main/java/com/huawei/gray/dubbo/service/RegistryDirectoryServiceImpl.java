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

package com.huawei.gray.dubbo.service;

import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.RuleStrategyEnum;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.utils.CollectionUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import java.util.List;

/**
 * RegistryDirectoryInterceptor的service
 *
 * @author provenceee
 * @since 2021/11/24
 */
public class RegistryDirectoryServiceImpl implements RegistryDirectoryService {
    @Override
    public Object selectInvokers(Object[] arguments, Object result) {
        if (arguments != null && arguments.length > 0 && arguments[0] instanceof Invocation) {
            GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getLabelName());
            if (GrayConfiguration.isInValid(grayConfiguration)) {
                return result;
            }
            Invocation invocation = (Invocation) arguments[0];
            URL requestUrl = invocation.getInvoker().getUrl();
            if (!RouterUtil.isConsumer(requestUrl)) {
                return result;
            }
            String targetService = RouterUtil.getTargetService(requestUrl);
            String interfaceName = requestUrl.getServiceInterface() + "." + invocation.getMethodName();
            List<Rule> rules = RouterUtil.getValidRules(grayConfiguration, targetService, interfaceName);
            List<Route> routes = RouterUtil.getRoutes(rules, invocation.getArguments());
            RuleStrategyEnum ruleStrategyEnum =
                CollectionUtils.isEmpty(routes) ? RuleStrategyEnum.UPSTREAM : RuleStrategyEnum.WEIGHT;
            return ruleStrategyEnum.getTargetInvoker(routes, invocation, (List<Invoker<?>>) result,
                grayConfiguration.getVersionFrom());
        }
        return result;
    }
}