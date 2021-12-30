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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.domain.HttpRequestEntity;
import com.huawei.flowrecord.domain.HttpResponseEntity;
import com.huawei.flowrecord.domain.RecordFlag;
import com.huawei.flowrecord.domain.RecordJob;
import com.huawei.flowrecord.domain.RecordStatus;
import com.huawei.flowrecord.domain.Recorder;
import com.huawei.flowrecord.utils.RecordUtil;
import com.huawei.flowrecord.utils.StreamUtil;
import com.huawei.flowrecord.utils.StringUtil;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import org.apache.commons.io.IOUtils;
import org.apache.curator.shaded.com.google.common.hash.Hashing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public class RestletServiceImpl extends RestletService{
    private final Logger LOGGER = LoggerFactory.getLogger();
    private static final String DOPOST_METHOD = "post";
    private final GatewayClient gatewayClient = ServiceManager.getService(GatewayClient.class);
    private static final String PATH = "";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (!DOPOST_METHOD.equals(method.getName())) {
            return;
        }

        if (!RecordUtil.isRecord(RecordJob.recordJob, method.getName(), PATH)) {
            RecordFlag.setIsRecord(false);
            return;
        }
        RecordFlag.setIsRecord(true);
        RecordFlag.setIsEntry(true);


        Class clazz = arguments[0].getClass();
        Method getStream = clazz.getDeclaredMethod("getStream");
        Method setStream = clazz.getDeclaredMethod("setStream", InputStream.class);
        InputStream inputStream = (InputStream) getStream.invoke(arguments[0]);
        ByteArrayOutputStream byteArrayOutputStream = StreamUtil.CloneInputStream(inputStream);
        InputStream stream1 = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        InputStream stream2 = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        setStream.invoke(arguments[0], stream1);
        String requestBody = IOUtils.toString(stream2, StandardCharsets.UTF_8);
        HttpRequestEntity httpRequestEntity = new HttpRequestEntity();
        httpRequestEntity.setMethod(method.getName());
        httpRequestEntity.setUrl(obj.toString());
        httpRequestEntity.setHttpRequestBody(requestBody);
        RecordStatus.httpContext.set(JSONObject.toJSONString(httpRequestEntity));
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (!DOPOST_METHOD.equals(method.getName())) {
            return ;
        }
        String requestStr = RecordStatus.httpContext.get();
        HttpRequestEntity httpRequestEntity = JSONObject.parseObject(requestStr, HttpRequestEntity.class);
        HttpResponseEntity httpResponseEntity = new HttpResponseEntity();
        int status = StringUtil.getRequestStatus(obj.toString());
        httpResponseEntity.setHttpResponseBody(result.toString());
        httpResponseEntity.setStatus(status);
        sendRecorder(method.getName(), PATH, httpRequestEntity, httpResponseEntity);
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        LOGGER.severe(t.getMessage());
    }


    public void sendRecorder(String httpMethod, String path, HttpRequestEntity requestEntity, HttpResponseEntity responseEntity) {
        Recorder recordData = Recorder.builder().build();
        recordData.setTraceId(TraceCollector.getVirtualTraceId());
        if (RecordJob.recordJob != null) {
            recordData.setJobId(RecordJob.recordJob.getJobId());
        }

        recordData.setMethodName(httpMethod + " " + path);
        recordData.setAppType("HTTP");
        recordData.setEntry(true);
        recordData.setTimestamp(new Date());
        String subCallKey = Hashing.sha256().hashString(TraceCollector.getVirtualTraceId() + requestEntity.getUrl(), StandardCharsets.UTF_8).toString();
        recordData.setSubCallKey(subCallKey);
        recordData.setSubCallCount(setSubCallCount(subCallKey));
        recordData.setRequestClass("java.lang.String");
        recordData.setRequestBody(JSONObject.toJSONString(requestEntity, SerializerFeature.WriteMapNullValue));
        recordData.setResponseClass("java.lang.Object");
        recordData.setResponseBody(JSONObject.toJSONString(responseEntity, SerializerFeature.WriteMapNullValue));
        String serializeData = JSONObject.toJSONString(recordData, SerializerFeature.WriteMapNullValue);
        gatewayClient.send(serializeData.getBytes(StandardCharsets.UTF_8), CommonConst.FLOW_RECORD_DATA_TYPE);
    }

    private int setSubCallCount(String subCallKey) {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        int subCallCount = 0;

        if (relationContext == null) {
            relationContext = new HashMap<String, String>();
            subCallCount++;
        }

        if (relationContext != null && relationContext.get(subCallKey) != null) {
            subCallCount = Integer.parseInt(relationContext.get(subCallKey));
            subCallCount++;
        }
        relationContext.put(subCallKey, String.valueOf(subCallCount));
        RecordStatus.relationContext.set(relationContext);
        return subCallCount;
    }
}
