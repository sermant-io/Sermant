/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.dubbo;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.*;
import com.lubanops.apm.plugin.flowreplay.mockclient.service.MockResultService;

import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;

import org.apache.dubbo.rpc.RpcContext;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Dubbo 应用 mock client
 *
 * @author luanwenfei
 * @version 0.0.1 2021-02-05
 * @since 2021-02-05
 */
public class DubboConsumerClientInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboConsumerClientInterceptor.class);

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        Method invokeMethod = (Method) arguments[1];
        RpcContext rpcContext = RpcContext.getContext();

        String methodName = invokeMethod.getDeclaringClass().getName() + "." + invokeMethod.getName();

        HashMap<String, String> relationContext = (HashMap<String, String>) MockStatus.relationContext.get();
        if (checkContextManager(methodName)) {
            String traceId = relationContext.get(PluginConfig.TRACE_ID);

            // 通过sha256算法提取 sub call key
            String subCallKey = Hashing.sha256().hashString(traceId + methodName
                    + JSON.toJSONString(invokeMethod.getGenericParameterTypes()), StandardCharsets.UTF_8).toString();

            // subCallKey计数
            int subCallCount = 0;
            if (relationContext.get(subCallKey) != null) {
                subCallCount = Integer.parseInt(relationContext.get(subCallKey));

                // 计数增加
                subCallCount++;
            }
            ContextManager.getCorrelationContext().put(subCallKey, String.valueOf(subCallCount));

            // 将参数序列化
            String args = JSON.toJSONString(arguments[2]);
            boolean isMock = PluginConfig.isMock;
            String recordJobId = relationContext.get(PluginConfig.RECORD_JOB_ID);
            if (isMock) {
                MockResultService mockResultService = new MockResultService();
                MockRequest mockRequest = new MockRequest(subCallKey,
                        subCallCount, recordJobId, PluginConfig.DUBBO, args, methodName);
                MockResult mockResult = mockResultService.getMockResult(mockRequest);
                if (mockResult != null && !mockResult.getMockRequestType().equals(PluginConfig.NOTYPE)) {
                    SelectResult selectResult = mockResult.getSelectResult();

                    // 通过类加载器加载类并进行反序列化
                    beforeResult.setResult(
                            JSON.parseObject(
                                    selectResult.getSelectContent(), ClassLoader.getSystemClassLoader()
                                            .loadClass(selectResult.getSelectClassName())
                            )
                    );
                    LOGGER.info("Mock successful , sub call key :{}", subCallKey);
                }
            }
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }

    /**
     * 检查 ContextManager 内容是否满足mock条件
     *
     * @param method 方法名
     * @return 返回ContextManager是否满足条件
     */
    private boolean checkContextManager(String method) {

        HashMap<String, String> relationContext = (HashMap<String, String>) MockStatus.relationContext.get();

        if (relationContext == null) {
            LOGGER.info("CorrelationContext is null , skip mock : {}", method);
            return false;
        }
        if (relationContext.get(PluginConfig.RECORD_JOB_ID) == null) {
            LOGGER.info("Record job id is null , skip mock : {}", method);
            return false;
        }
        if (relationContext.get(PluginConfig.TRACE_ID) == null) {
            LOGGER.info("Trace id is null , skip mock : {}", method);
            return false;
        }
        return true;
    }
}
