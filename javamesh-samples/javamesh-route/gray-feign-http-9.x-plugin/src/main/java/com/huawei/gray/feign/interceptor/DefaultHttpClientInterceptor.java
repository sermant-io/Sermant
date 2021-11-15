/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
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
 * 拦截feign执行http请求的execute方法，匹配标签规则进行灰度路由
 *
 * @author lilai
 * @since 2021-11-03
 */
public class DefaultHttpClientInterceptor implements InstanceMethodInterceptor {

    /**
     * 获取当前服务信息
     *
     * @param obj          拦截对象
     * @param method       拦截方法
     * @param arguments    方法参数
     * @param beforeResult change this result, if you want to truncate the method.
     */
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
        FeignResolvedURL feignResolvedURL = PathVarInterceptor.URL_CONTEXT.get();
        if (feignResolvedURL != null) {
            try {
                path = path.replace(feignResolvedURL.getUrl().split("[?]")[0],
                        feignResolvedURL.getOriginUrl()).split("[?]")[0];
            } finally {
                PathVarInterceptor.URL_CONTEXT.remove();
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
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        HostContext.remove();
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        HostContext.remove();
    }
}
