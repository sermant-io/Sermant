/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.service;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.util.SpiLoadUtil.SpiWeight;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.RuleStrategyEnum;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.addr.entity.Metadata;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.threadlocal.ThreadLocalContext;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DubboInvokerInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
@SpiWeight(1)
public class DubboInvokerServiceImpl extends DubboInvokerService {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String REQUEST_URL_KEY = "requestUrl";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        Invocation invocation = (Invocation) arguments[0];
        URL requestUrl = invocation.getInvoker().getUrl();
        if (ThreadLocalContext.INSTANCE.get(REQUEST_URL_KEY) == null) {
            ThreadLocalContext.INSTANCE.put(REQUEST_URL_KEY, requestUrl);
        }
        String targetService = RouterUtil.getTargetService(requestUrl);
        GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getLabelName());
        String targetVersion = null;
        String group = null;
        String clusterName = null;
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return;
        }
        if (RpcContext.getContext().isConsumerSide()) {
            String version = grayConfiguration.getCurrentTag().getValidVersion(DubboCache.getLabelName());
            String interfaceName = requestUrl.getServiceInterface() + "." + invocation.getMethodName();
            List<Rule> rules = RouterUtil.getValidRules(grayConfiguration, targetService, interfaceName);
            List<Route> routes = RouterUtil.getRoutes(rules, invocation.getArguments());
            RuleStrategyEnum ruleStrategyEnum =
                    CollectionUtils.isEmpty(routes) ? RuleStrategyEnum.UPSTREAM : RuleStrategyEnum.WEIGHT;
            String targetServiceIp = ruleStrategyEnum.getTargetServiceIp(routes, targetService, interfaceName, version,
                    invocation);
            Instances instance = AddrCache.getInstance(targetService, targetServiceIp);
            if (instance != null && instance.getMetadata() != null) {
                Metadata metadata = instance.getMetadata();
                targetVersion = metadata.getVersion();
                group = metadata.getGroup();
                clusterName = metadata.getClusterName();
            }
            RouterUtil.changeInvokerClients(invocation, targetServiceIp, requestUrl, targetVersion, group, clusterName);
        }
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        Result rpcResult = (Result) result;
        if (rpcResult != null && rpcResult.getException() != null) {
            LOGGER.log(Level.SEVERE, "DubboInvoker is error!", rpcResult.getException());
        }
    }
}