/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.dubbo;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcContext;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Alibaba dubbo provider 增强插件,用于在mock过程中传递上下文Context
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-28
 */
public class AlibabaDubboProviderClientInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlibabaDubboProviderClientInterceptor.class);

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        Invocation invocation = (Invocation) arguments[1];
        RpcContext rpcContext = RpcContext.getContext();
        boolean isProvider = rpcContext.isProviderSide();

        HashMap<String, String> relationContext = (HashMap<String, String>) MockStatus.relationContext.get();

        if (isProvider) {
            if (relationContext != null
                    && relationContext.get(PluginConfig.RECORD_JOB_ID) == null) {
                relationContext.put(PluginConfig.RECORD_JOB_ID,
                        rpcContext.getAttachment(PluginConfig.RECORD_JOB_ID));
                LOGGER.info("Set record job id in context manager: {}",
                        rpcContext.getAttachment(PluginConfig.RECORD_JOB_ID));
            }

            if (relationContext != null
                    && relationContext.get(PluginConfig.TRACE_ID) == null) {
                relationContext.put(PluginConfig.TRACE_ID,
                        rpcContext.getAttachment(PluginConfig.TRACE_ID));
                LOGGER.info("Set trace id in context manager : {}",
                        rpcContext.getAttachment(PluginConfig.TRACE_ID));
            }
        }
        MockStatus.relationContext.set(relationContext);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
