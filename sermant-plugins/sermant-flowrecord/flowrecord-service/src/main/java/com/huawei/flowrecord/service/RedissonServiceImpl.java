/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecord.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.domain.RecordStatus;
import com.huawei.flowrecord.domain.Recorder;
import com.huawei.flowrecord.utils.RedissonProcessThreadPool;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import lombok.SneakyThrows;
import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.redisson.misc.RedissonPromise;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public class RedissonServiceImpl extends RedissonService{

    private static final Logger LOGGER = LoggerFactory.getLogger();
    private final GatewayClient gatewayClient = ServiceManager.getService(GatewayClient.class);

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
            recordRequest.setAppType(CommonConst.REDISSON_APP_TYPE);
            recordRequest.setEntry(false);
            recordRequest.setRequestBody(JSON.toJSONString(params));
            Object result = ((RedissonPromise) ret).getNow();
            recordRequest.setResponseBody(JSON.toJSONString(result, SerializerFeature.WriteMapNullValue));
            recordRequest.setRequestClass("org.redisson.command.CommandAsyncService");
            recordRequest.setResponseClass(result.getClass().getName());
            recordRequest.setTimestamp(new Date());
            String serializedRequest = JSON.toJSONString(recordRequest, SerializerFeature.WriteMapNullValue);
            gatewayClient.send(serializedRequest.getBytes(StandardCharsets.UTF_8), CommonConst.FLOW_RECORD_DATA_TYPE);
        }
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
    }

    @Override
    public void after(Object obj, final Method method, Object[] arguments, final Object result) throws Exception {

        final HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

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
            final String subCallKey = Hashing.sha256().hashString(TraceCollector.getVirtualTraceId() + methodName
                    + JSON.toJSONString(parameterTypes), StandardCharsets.UTF_8).toString();
            LOGGER.info("Get subCallKey successful: " + subCallKey);

            // subCallKey的调用计数
            final int subCallCount = setSubCallCount(subCallKey);

            final Object[] finalParams = params;
            final String traceId = TraceCollector.getVirtualTraceId();

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
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        LOGGER.severe("[RedissonInterceptor] exception：" + t.getMessage());
    }

}
