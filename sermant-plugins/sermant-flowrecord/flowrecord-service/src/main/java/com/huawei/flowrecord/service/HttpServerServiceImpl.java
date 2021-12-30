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
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.domain.HttpRequestEntity;
import com.huawei.flowrecord.domain.HttpResponseEntity;
import com.huawei.flowrecord.domain.RecordFlag;
import com.huawei.flowrecord.domain.RecordJob;
import com.huawei.flowrecord.domain.RecordStatus;
import com.huawei.flowrecord.domain.Recorder;
import com.huawei.flowrecord.utils.HttpUtil;
import com.huawei.flowrecord.utils.RecordUtil;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import org.apache.curator.shaded.com.google.common.hash.Hashing;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpServerServiceImpl extends HttpServerService {
    private static final String DOSERVICE_METHOD = "doService";
    private final GatewayClient gatewayClient = ServiceManager.getService(GatewayClient.class);

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (!DOSERVICE_METHOD.equals(method.getName())) {
            return;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) arguments[0];

        String path = "";
        if (!RecordUtil.isRecord(RecordJob.recordJob, httpServletRequest.getMethod(), path)) {
            RecordFlag.setIsRecord(false);
            return;
        }
        RecordFlag.setIsRecord(true);
        RecordFlag.setIsEntry(true);

        //读取inputStream, 并重新赋值inputStream
        ServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(httpServletRequest);
        String body = HttpUtil.getBodyString(requestWrapper);
        RecordStatus.httpContext.set(body);
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (!DOSERVICE_METHOD.equals(method.getName())) {
            return ;
        }

        if (!RecordFlag.getIsRecord() || !RecordFlag.getIsEntry()) {
            return ;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) arguments[0];
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headerMap.put(header, httpServletRequest.getHeader(header));
        }
        HttpRequestEntity httpRequestEntity = new HttpRequestEntity();
        if (httpServletRequest.getQueryString() != null) {
            httpRequestEntity.setUrl(httpServletRequest.getRequestURI() + "?" + httpServletRequest.getQueryString());
        } else {
            httpRequestEntity.setUrl(httpServletRequest.getRequestURI());
        }

        httpRequestEntity.setMethod(httpServletRequest.getMethod());
        httpRequestEntity.setHeadMap(headerMap);
        httpRequestEntity.setHttpRequestBody(RecordStatus.httpContext.get());
        httpRequestEntity.setParams(httpServletRequest.getParameterMap());

        HttpServletResponse httpServletResponse = (HttpServletResponse) arguments[1];
        HttpResponseEntity httpResponseEntity = new HttpResponseEntity();
        httpResponseEntity.setStatus(httpServletResponse.getStatus());
        httpResponseEntity.setHttpResponseBody(result);

        String path = "";
        if (httpServletResponse.getStatus() == CommonConst.SERVICE_NOT_EXIST) {
            path = "";
        }

        sendRecorder(httpServletRequest.getMethod(), path, httpRequestEntity, httpResponseEntity);

        return ;
    }

    @Override
    public void onThrow(Object obj, Method Enumeration, Object[] arguments, Throwable t) {

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
        recordData.setRequestBody(JSONObject.toJSONString(requestEntity));
        recordData.setResponseClass("java.lang.Object");
        recordData.setResponseBody(JSONObject.toJSONString(responseEntity));
        String serializeData = JSONObject.toJSONString(recordData);
        gatewayClient.send(serializeData.getBytes(StandardCharsets.UTF_8), CommonConst.FLOW_RECORD_DATA_TYPE);
    }
}
