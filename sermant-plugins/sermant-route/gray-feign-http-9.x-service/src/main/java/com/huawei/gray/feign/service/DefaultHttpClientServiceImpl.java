/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/plugin/feign/http/v9/DefaultHttpClientInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.gray.feign.service;

import com.huawei.gray.feign.context.FeignResolvedUrl;
import com.huawei.gray.feign.context.HostContext;
import com.huawei.gray.feign.rule.RuleType;
import com.huawei.gray.feign.util.RouterUtil;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import feign.Request;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * DefaultHttpClientInterceptor的service
 *
 * @author provenceee
 * @since 2021/11/26
 */
public class DefaultHttpClientServiceImpl implements DefaultHttpClientService {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        GrayConfig grayConfig = PluginConfigManager.getPluginConfig(GrayConfig.class);
        Request request = (Request) arguments[0];
        String targetAppName = HostContext.get();

        // 根据灰度规则重构请求地址
        GrayConfiguration grayConfiguration = LabelCache.getLabel(grayConfig.getSpringCloudKey());
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return;
        }

        // 获得url路径参数解析前的原始path
        URL url = new URL(request.url());
        String path = url.getPath();
        FeignResolvedUrl feignResolvedUrl = PathVarServiceImpl.URL_CONTEXT.get();
        if (feignResolvedUrl != null) {
            try {
                path = path.replace(feignResolvedUrl.getUrl().split("[?]")[0],
                    feignResolvedUrl.getOriginUrl()).split("[?]")[0];
            } finally {
                PathVarServiceImpl.URL_CONTEXT.remove();
            }
        }

        // 获取匹配规则并替换url
        List<Rule> rules = RouterUtil.getValidRules(grayConfiguration, targetAppName, path);
        List<Route> routes = RouterUtil.getRoutes(rules, request);
        RuleType ruleType = CollectionUtils.isEmpty(routes) ? RuleType.UPSTREAM : RuleType.WEIGHT;
        Instances instance = ruleType.getTargetServiceInstance(routes, targetAppName,
            request.headers());
        if (instance != null) {
            String targetServiceHost = RouterUtil.getTargetHost(instance);
            String version = instance.getCurrentTag().getVersion();
            request = RouterUtil.rebuildUrl(targetServiceHost, version, request);
            arguments[0] = request;
        }
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        HostContext.remove();
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        HostContext.remove();
    }
}
