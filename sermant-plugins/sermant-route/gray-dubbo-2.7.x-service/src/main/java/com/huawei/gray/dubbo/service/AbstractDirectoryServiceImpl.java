/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huawei.gray.dubbo.strategy.RuleStrategyHandler;
import com.huawei.gray.dubbo.utils.ReflectUtils;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.utils.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * AbstractDirectory的service
 *
 * @author provenceee
 * @since 2021-11-24
 */
public class AbstractDirectoryServiceImpl implements AbstractDirectoryService {
    // dubbo请求参数中是否为consumer的key值
    private static final String CONSUMER_KEY = "side";

    // dubbo请求参数中接口名的key值
    private static final String INTERFACE_KEY = "interface";

    // dubbo请求参数中是否为consumer的value值
    private static final String CONSUMER_VALUE = "consumer";

    /**
     * 筛选灰度invoker
     *
     * @param obj RegistryDirectory
     * @param arguments 参数
     * @param result invokers
     * @return invokers
     * @see com.alibaba.dubbo.registry.integration.RegistryDirectory
     * @see org.apache.dubbo.registry.integration.RegistryDirectory
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     */
    @Override
    public Object selectInvokers(Object obj, Object[] arguments, Object result) {
        if (arguments != null && arguments.length > 0) {
            GrayConfiguration grayConfiguration = LabelCache.getLabel(GrayConstant.GRAY_LABEL_CACHE_NAME);
            if (GrayConfiguration.isInValid(grayConfiguration)) {
                return result;
            }
            Map<String, String> queryMap = ReflectUtils.getQueryMap(obj);
            if (CollectionUtils.isEmpty(queryMap)) {
                return result;
            }
            if (!CONSUMER_VALUE.equals(queryMap.get(CONSUMER_KEY))) {
                return result;
            }
            String serviceInterface = queryMap.get(INTERFACE_KEY);
            Object invocation = arguments[0];
            String interfaceName = serviceInterface + "." + ReflectUtils.getMethodName(invocation);
            String targetService = DubboCache.INSTANCE.getApplication(serviceInterface);
            List<Rule> rules = RouterUtil.getValidRules(grayConfiguration, targetService, interfaceName);
            List<Route> routes = RouterUtil.getRoutes(rules, ReflectUtils.getArguments(invocation));
            if (CollectionUtils.isEmpty(routes)) {
                return result;
            }
            return RuleStrategyHandler.WEIGHT.getTargetInvoker(routes, (List<Object>) result,
                grayConfiguration.getVersionFrom());
        }
        return result;
    }
}