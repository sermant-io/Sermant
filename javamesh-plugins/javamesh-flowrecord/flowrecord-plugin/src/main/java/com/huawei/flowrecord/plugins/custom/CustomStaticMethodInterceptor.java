/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowrecord.plugins.custom;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.javamesh.core.service.send.GatewayClient;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.config.ConfigConst;
import com.huawei.flowrecord.domain.RecordStatus;
import com.huawei.flowrecord.domain.Recorder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

/**
 * 自定义应用静态方法增强类
 */
public class CustomStaticMethodInterceptor implements StaticMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomStaticMethodInterceptor.class);
    private final GatewayClient gatewayClient = ServiceManager.getService(GatewayClient.class);

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
            gatewayClient.send(serializedRequest.getBytes(StandardCharsets.UTF_8), CommonConst.FLOW_RECORD_DATA_TYPE);
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
