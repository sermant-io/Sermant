/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.plugins.redisson.v3;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.trace.TraceCollector;
import com.huawei.flowrecord.config.ConfigConst;
import com.huawei.flowrecord.domain.RecordStatus;
import com.huawei.flowrecord.domain.Recorder;
import com.huawei.flowrecord.init.RedissonProcessThreadPool;
import com.huawei.flowrecord.utils.PluginConfigUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.huawei.flowrecord.utils.KafkaProducerUtil;
import lombok.SneakyThrows;

import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.redisson.misc.RedissonPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

/**
 * redisson拦截增强类
 *
 */
public class RedissonInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonInterceptor.class);

    public int setSubCallCount(String subCallKey) {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        int subCallCount = 0;
        if (relationContext.get(subCallKey) != null) {
            subCallCount = Integer.parseInt(relationContext.get(subCallKey));
            subCallCount++;
        }
        relationContext.put(subCallKey, String.valueOf(subCallCount));
        RecordStatus.relationContext.set(relationContext);
        return subCallCount;
    }

    private void sendRecorder(String subCallKey, int subCallCount, Method method, Object[] params, Object ret,
                              HashMap<String, String> jobMap, String traceId) {
        for (String jobId : jobMap.keySet()) {
            Recorder recordRequest = Recorder.builder().build();
            recordRequest.setSubCallKey(subCallKey);
            recordRequest.setSubCallCount(subCallCount);
            recordRequest.setTraceId(traceId);
            recordRequest.setJobId(jobId);
            recordRequest.setMethodName(method.getDeclaringClass().getName() + "." + method.getName());
            recordRequest.setAppType(ConfigConst.REDISSON_APP_TYPE);
            recordRequest.setEntry(false);
            recordRequest.setRequestBody(JSON.toJSONString(params));
            Object result = ((RedissonPromise) ret).getNow();
            recordRequest.setResponseBody(JSON.toJSONString(result, SerializerFeature.WriteMapNullValue));
            recordRequest.setRequestClass("org.redisson.command.CommandAsyncService");
            recordRequest.setResponseClass(result.getClass().getName());
            recordRequest.setTimestamp(new Date());
            String serializedRequest = JSON.toJSONString(recordRequest, SerializerFeature.WriteMapNullValue);
            KafkaProducerUtil.sendMessage(PluginConfigUtil.getValueByKey(ConfigConst.KAFKA_REQUEST_TOPIC), serializedRequest);
        }
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        if (relationContext != null && relationContext.get("recordJobList") != null) {
            Object[] params = null;
            String[] parameterTypes = null;

            // 取出参数类型
            if (arguments.length >= 1 && arguments[arguments.length - 1] instanceof Object[]) {
                params = (Object[]) arguments[arguments.length - 1];
                parameterTypes = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    parameterTypes[i] = params[i].getClass().getName();
                }
            }

            // 生成subCallKey
            String methodName = method.getDeclaringClass().getName() + "." + method.getName();
            String subCallKey = Hashing.sha256().hashString(TraceCollector.getVirtualTraceId() + methodName
                    + JSON.toJSONString(parameterTypes), StandardCharsets.UTF_8).toString();
            LOGGER.info("Get subCallKey successful: {}", subCallKey);

            // subCallKey的调用计数
            int subCallCount = setSubCallCount(subCallKey);

            Object[] finalParams = params;
            String traceId = TraceCollector.getVirtualTraceId();

            // 多线程处理异步请求的等待过程和数据发送过程
            RedissonProcessThreadPool.getInstance().executeTask(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    // 异步请求未完成则等待请求完成
                    if (!((RedissonPromise) result).isSuccess()) {
                        ((RedissonPromise) result).await();
                    }

                    // 录制数据发送至kafka
                    HashMap<String, String> jobMap = JSON.parseObject(relationContext.get("recordJobList"), new TypeReference<HashMap<String, String>>() {
                    });
                    sendRecorder(subCallKey, subCallCount, method, finalParams, result, jobMap, traceId);
                    LOGGER.info("send redisson data to kafka successful");
                }
            });
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        LOGGER.error("[RedissonInterceptor] exception：" + t.getMessage());
    }
}
