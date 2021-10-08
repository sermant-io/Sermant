/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.redisson;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.*;
import com.lubanops.apm.plugin.flowreplay.mockclient.service.MockResultService;

import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;
import org.redisson.misc.RedissonPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 拦截Redisson后进行mock
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-06
 */
public class RedissonClientInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonClientInterceptor.class);

    /**
     * 检查mock result是否符合mock条件
     *
     * @param mockResult mock server返回的结果
     * @param subCallKey 子调用的标识key
     * @return 返回是否进行mock
     */
    private boolean checkMockResult(MockResult mockResult, String subCallKey) {
        if (mockResult == null) {
            LOGGER.info("Mock result is null , skip mock : {}", subCallKey);
            return false;
        }
        if (mockResult.getMockRequestType().equals(PluginConfig.NOTYPE)) {
            LOGGER.info("Type is not supported , skip mock : {}", subCallKey);
            return false;
        }
        if (mockResult.getMockAction().equals(MockAction.SKIP)) {
            LOGGER.info("Mock action is SKIP , skip mock : {}", subCallKey);
            return false;
        }
        if (mockResult.getSelectResult() == null) {
            LOGGER.info("Select result is null  , skip mock : {}", subCallKey);
            return false;
        }
        if (mockResult.getSelectResult().getSelectContent() == null
                || mockResult.getSelectResult().getSelectClassName() == null) {
            LOGGER.info("Select result is null , skip mock : {}", subCallKey);
            return false;
        }
        return true;
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

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
        Object[] params = null;
        String[] paramsTypes = null;
        if (arguments.length > 1 && arguments[arguments.length - 1] instanceof Object[]) {
            params = (Object[]) arguments[arguments.length - 1];
            paramsTypes = new String[params.length];
            for (int param = 0; param < params.length; param++) {
                paramsTypes[param] = params[param].getClass().getName();
            }
        }

        HashMap<String, String> relationContext = (HashMap<String, String>) MockStatus.relationContext.get();
        if (checkContextManager(methodName)) {
            String traceId = relationContext.get(PluginConfig.TRACE_ID);

            // 通过sha256算法提取 sub call key
            String subCallKey = Hashing.sha256().hashString(traceId + methodName
                            + JSON.toJSONString(paramsTypes),
                    StandardCharsets.UTF_8).toString();

            // subCallKey计数
            int subCallCount = 0;
            if (relationContext.get(subCallKey) != null) {
                subCallCount = Integer
                        .parseInt(relationContext.get(subCallKey));

                // 计数增加
                subCallCount++;
            }
            relationContext.put(subCallKey, String.valueOf(subCallCount));

            // 将参数序列化
            String args = JSON.toJSONString(params);
            boolean isMock = PluginConfig.isMock;
            String recordJobId = relationContext.get(PluginConfig.RECORD_JOB_ID);
            if (isMock) {
                MockResultService mockResultService = new MockResultService();
                MockRequest mockRequest = new MockRequest(subCallKey,
                        subCallCount, recordJobId,
                        PluginConfig.REDIS, args, methodName);
                MockResult mockResult = mockResultService.getMockResult(mockRequest);
                if (checkMockResult(mockResult, subCallKey)) {
                    SelectResult selectResult = mockResult.getSelectResult();

                    // 利用Redisson RedissonPromise将反序列化后的对象包装
                    RedissonPromise<Object> rFuture = new RedissonPromise<>();
                    rFuture.trySuccess(JSON.parseObject(
                            selectResult.getSelectContent(), ClassLoader.getSystemClassLoader()
                                    .loadClass(selectResult.getSelectClassName())
                    ));
                    beforeResult.setResult(rFuture);
                    LOGGER.info("Mock successful , sub call key :{}", subCallKey);
                }
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
