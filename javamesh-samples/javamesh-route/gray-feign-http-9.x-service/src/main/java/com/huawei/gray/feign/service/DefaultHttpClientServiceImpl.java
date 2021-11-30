/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.feign.service;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.gray.feign.context.CurrentInstance;
import com.huawei.gray.feign.context.FeignResolvedURL;
import com.huawei.gray.feign.context.HostContext;
import com.huawei.gray.feign.rule.RuleType;
import com.huawei.gray.feign.util.RouterUtil;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;

import feign.Request;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * DefaultHttpClientInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/26
 */
public class DefaultHttpClientServiceImpl implements DefaultHttpClientService {
    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        Request request = (Request) arguments[0];
        String targetAppName = HostContext.get();

        // 根据灰度规则重构请求地址
        GrayConfiguration grayConfiguration = LabelCache.getLabel(CurrentInstance.getInstance().getAppName());
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return;
        }

        // 获得url路径参数解析前的原始path
        URL url = new URL(request.url());
        String path = url.getPath();
        FeignResolvedURL feignResolvedURL = PathVarServiceImpl.URL_CONTEXT.get();
        if (feignResolvedURL != null) {
            try {
                path = path.replace(feignResolvedURL.getUrl().split("[?]")[0],
                        feignResolvedURL.getOriginUrl()).split("[?]")[0];
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
