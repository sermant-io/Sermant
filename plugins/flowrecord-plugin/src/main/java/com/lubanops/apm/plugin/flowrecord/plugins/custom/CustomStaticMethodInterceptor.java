/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.custom;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.lubanops.apm.bootstrap.trace.TraceCollector;
import com.lubanops.apm.plugin.flowrecord.config.ConfigConst;
import com.lubanops.apm.plugin.flowrecord.domain.RecordStatus;
import com.lubanops.apm.plugin.flowrecord.domain.Recorder;
import com.lubanops.apm.plugin.flowrecord.utils.PluginConfigUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.lubanops.apm.plugin.flowrecord.utils.KafkaProducerUtil;
import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

/**
 * 自定义应用静态方法增强类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-05-10
 */
public class CustomStaticMethodInterceptor implements StaticMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomStaticMethodInterceptor.class);

    private void sendRecorder(String subCallKey, int subCallCount, Method method, Object[] params, Object ret,
                              HashMap<String, String> jobMap) {
        for (String jobId : jobMap.keySet()) {
            Recorder recordRequest = Recorder.builder().build();
            recordRequest.setSubCallKey(subCallKey);
            recordRequest.setSubCallCount(subCallCount);
            recordRequest.setTraceId(TraceCollector.getVirtualTraceId());
            recordRequest.setJobId(jobId);
            recordRequest.setMethodName(method.getDeclaringClass().getName() + "." + method.getName());
            recordRequest.setAppType(ConfigConst.CUSTOM_APP_TYPE);
            recordRequest.setEntry(false);
            recordRequest.setRequestBody(JSON.toJSONString(params, SerializerFeature.WriteMapNullValue));
            recordRequest.setResponseBody(JSON.toJSONString(ret, SerializerFeature.WriteMapNullValue));
            recordRequest.setRequestClass(method.getClass().getName());
            recordRequest.setResponseClass(ret.getClass().getName());
            recordRequest.setTimestamp(new Date());
            String serializedRequest = JSON.toJSONString(recordRequest, SerializerFeature.WriteMapNullValue);
            KafkaProducerUtil.sendMessage(PluginConfigUtil.getValueByKey(ConfigConst.KAFKA_REQUEST_TOPIC), serializedRequest);
        }
    }

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

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        if (relationContext != null && relationContext.get("recordJobList") != null) {
            // 生成subCallKey
            String methodName = method.getDeclaringClass().getName() + "." + method.getName();
            String subCallKey = Hashing.sha256().hashString(TraceCollector.getVirtualTraceId() + methodName,
                    StandardCharsets.UTF_8).toString();
            LOGGER.info("Get subCallKey successful: {}", subCallKey);

            // subCallKey的调用计数
            int subCallCount = setSubCallCount(subCallKey);

            // 录制数据发送至kafka
            HashMap<String, String> jobMap = JSON.parseObject(relationContext.get("recordJobList"), new TypeReference<HashMap<String, String>>() {
            });
            sendRecorder(subCallKey, subCallCount, method, arguments, result, jobMap);
        }
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
