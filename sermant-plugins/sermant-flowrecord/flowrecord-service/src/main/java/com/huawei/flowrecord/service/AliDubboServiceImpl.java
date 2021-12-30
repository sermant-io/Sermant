/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/plugin/dubbo/DubboInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.flowrecord.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.domain.RecordContext;
import com.huawei.flowrecord.domain.RecordJob;
import com.huawei.flowrecord.domain.RecordStatus;
import com.huawei.flowrecord.domain.Recorder;
import com.huawei.flowrecord.utils.DubboUtil;
import com.huawei.flowrecord.utils.RecordUtil;
import com.huawei.flowrecord.utils.StringUtil;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.TransformAccess;
import com.huawei.sermant.core.lubanops.bootstrap.collector.api.FutureStatsAccessor;
import com.huawei.sermant.core.lubanops.bootstrap.trace.Headers;
import com.huawei.sermant.core.lubanops.bootstrap.trace.SpanEvent;
import com.huawei.sermant.core.lubanops.bootstrap.trace.StartTraceRequest;
import com.huawei.sermant.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.apache.dubbo.common.constants.CommonConstants;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.support.RpcUtils;

public class AliDubboServiceImpl extends AliDubboService {
    private final Logger LOGGER = LoggerFactory.getLogger();
    public static final String CONSUMER_TAG = "DUBBO_CONSUMER";
    public static final String PROVIDER_TAG = "DUBBO_PROVIDER";
    public static final String PATH = "";

    private final GatewayClient gatewayClient = ServiceManager.getService(GatewayClient.class);
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        Invoker invoker = (Invoker) arguments[0];
        Invocation invocation = (Invocation) arguments[1];
        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();
        URL requestUrl = invoker.getUrl();

        String side = requestUrl.getParameter(CommonConstants.SIDE_KEY);
        String protocol = requestUrl.getProtocol();
        String application = requestUrl.getParameter(CommonConstants.APPLICATION_KEY);
        String interfaceName = invoker.getInterface().getName();
        String group = requestUrl.getParameter(CommonConstants.GROUP_KEY);
        String version = requestUrl.getParameter(CommonConstants.VERSION_KEY);
        String className = obj.getClass().getName();
        String methodName = method.getName();
        String serviceUniqueName = DubboUtil.buildServiceKey(interfaceName, group, version);
        String source = DubboUtil.buildSourceKey(serviceUniqueName, methodName);

        SpanEvent apmSpanEvent;
        boolean start;
        try {
            start = RecordUtil.isRecord(RecordJob.recordJob, invocation.getMethodName(), PATH);
        } catch (Exception e) {
            start = false;
        }
        CommonConst.VALUE_MAX_LENGTH = 1280;
        CommonConst.ELEMENT_MAX_NUMBER = 10;

        injectRecordContext(isConsumer, invocation);

        if (isConsumer) {
            apmSpanEvent = processConsumer(className, methodName, invocation, source);
        } else {
            apmSpanEvent = processProvider(className, methodName, invocation, source);
            if (start) {
                correlationInsert(invoker, invocation);
            }
        }
        transformAccess(invoker, invocation, serviceUniqueName, methodName, apmSpanEvent, side, protocol, application);
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        Invoker invoker = (Invoker) arguments[0];
        Invocation invocation = (Invocation) arguments[1];
        boolean start;

        if (RecordJob.recordJob != null) {
            start = RecordUtil.isRecord(RecordJob.recordJob, invocation.getMethodName(), PATH);
        } else {
            start = false;
        }

        RecordContext recordContext;
        Stack<RecordContext> recordContextStack = (Stack<RecordContext>) RecordStatus.context.get();
        recordContext = recordContextStack.pop();

        for (Map.Entry<String, String> entry : RecordStatus.map.entrySet()) {
            try {
                if (entry.getKey() == RecordJob.recordJob.getJobId() && start) {
                    if (entry.getValue().equals(invoker.getInterface().getName() + CommonConst.POINT_SIGN +
                            invocation.getMethodName())) {
                        //entry
                        sendRecorder(invocation, (Result) result, true, invoker, entry.getKey(), recordContext);

                    } else {
                        //subcall
                        sendRecorder(invocation, (Result) result, false, invoker, entry.getKey(), recordContext);
                    }
                } else if (entry.getKey() != RecordJob.recordJob.getJobId()) {
                    if (recordContext.isConsumer) {
                        sendRecorder(invocation, (Result) result, false, invoker, entry.getKey(), recordContext);
                    }
                }
            } catch (Throwable throwable) {
                LOGGER.severe("sendRecorder error: " + throwable.getMessage());
            }

        }
        TraceCollector.onFinally();
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }

    private void sendRecorder(Invocation invocation, Result ret, boolean entry, Invoker invoker, String jobId,
                              RecordContext recordContext) throws Throwable {
        Recorder recordRequest = Recorder.builder().build();
        recordRequest.setTraceId(TraceCollector.getVirtualTraceId());
        recordRequest.setJobId(jobId);
        recordRequest.setMethodName(invoker.getInterface().getCanonicalName() + CommonConst.POINT_SIGN +
                invocation.getMethodName());
        recordRequest.setAppType(CommonConst.DUBBO_APP_TYPE);
        recordRequest.setEntry(entry);
        recordRequest.setTimestamp(new Date());
        String subcall = TraceCollector.getVirtualTraceId() + invoker.getInterface().getCanonicalName() +
                CommonConst.POINT_SIGN + invocation.getMethodName() + JSON.toJSONString(invocation.getParameterTypes());
        String subcallWithSha256 = Hashing.sha256().hashString(subcall, StandardCharsets.UTF_8).toString();
        recordRequest.setSubCallKey(subcallWithSha256);
        int subcallcount = setSubCallCount(subcall);
        recordRequest.setSubCallCount(subcallcount);
        String req = recordContext.requestBody;
        Result result = (Result) ret;
        result.recreate();
        String res = JSON.toJSONString(result.recreate(), SerializerFeature.WriteMapNullValue);
        recordRequest.setRequestBody(req);
        recordRequest.setResponseBody(res);
        recordRequest.setRequestClass(recordContext.requestClass);
        recordRequest.setResponseClass(result.recreate().getClass().getName());
        String serializedrequest = JSON.toJSONString(recordRequest, SerializerFeature.WriteMapNullValue);
        gatewayClient.send(serializedrequest.getBytes(StandardCharsets.UTF_8), CommonConst.FLOW_RECORD_DATA_TYPE);
    }

    private void correlationInsert(Invoker invoker, Invocation invocation) {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        if (relationContext == null || relationContext.get(CommonConst.RECORD_LIST) == null) {
            RecordStatus.map.put(RecordJob.recordJob.getJobId(), invoker.getInterface().getName() + CommonConst.POINT_SIGN
                    + invocation.getMethodName());
            String map = JSON.toJSONString(RecordStatus.map);
            relationContext = new HashMap<String, String>();
            relationContext.put(CommonConst.RECORD_LIST, map);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(relationContext.get(CommonConst.RECORD_LIST));
            RecordStatus.map = (Map) jsonObject;
            RecordStatus.map.put(RecordJob.recordJob.getJobId(), invoker.getInterface().getName() + CommonConst.POINT_SIGN
                    + invocation.getMethodName());
            String map = JSON.toJSONString(RecordStatus.map);
            relationContext.put(CommonConst.RECORD_LIST, map);
        }
        RecordStatus.relationContext.set(relationContext);
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

    private void injectRecordContext(boolean isConsumer, Invocation invocation) {
        RecordContext recordContext = new RecordContext();
        if (RecordStatus.context.get() == null) {
            Stack<RecordContext> recordContextStack = new Stack<RecordContext>();
            recordContext.isConsumer = isConsumer;
            recordContext.requestBody = JSON.toJSONString(invocation, SerializerFeature.WriteMapNullValue);
            recordContext.requestClass = Invocation.class.getCanonicalName();
            recordContextStack.push(recordContext);
            RecordStatus.context.set(recordContextStack);
        } else {
            Stack<RecordContext> recordContextStack = (Stack<RecordContext>) RecordStatus.context.get();
            recordContext.isConsumer = isConsumer;
            recordContext.requestBody = JSON.toJSONString(invocation, SerializerFeature.WriteMapNullValue);
            recordContext.requestClass = Invocation.class.getCanonicalName();
            recordContextStack.push(recordContext);
            RecordStatus.context.set(recordContextStack);
        }
    }

    private SpanEvent processConsumer(String className, String methodName, Invocation invocation, String source) {
        SpanEvent apmSpanEvent = TraceCollector.getSpanEvent();
        if (apmSpanEvent != null) {
            apmSpanEvent = TraceCollector.onStart(className, methodName, AliDubboServiceImpl.CONSUMER_TAG);
        }
        if (apmSpanEvent != null) {
            invocation.getAttachments().put(Headers.TRACE_ID.getValue(), apmSpanEvent.getTraceId());
            invocation.getAttachments().put(Headers.SPAN_ID.getValue(), apmSpanEvent.generateNextSpanId());
            apmSpanEvent.addTag("url", source);
        } else {
            String apmVTraceId = TraceCollector.getVirtualTraceId();
            if (apmVTraceId != null) {
                invocation.getAttachments().put(Headers.GTRACE_ID.getValue(), apmVTraceId);
            }
        }
        return apmSpanEvent;
    }

    private SpanEvent processProvider(String className, String methodName, Invocation invocation, String source) {
        SpanEvent apmSpanEvent;
        String apmTraceId = invocation.getAttachment(Headers.TRACE_ID.getValue());
        String apmSpanId = invocation.getAttachment(Headers.SPAN_ID.getValue());
        String apmGTraceId = null;
        if (apmTraceId == null || StringUtil.isBlank(apmTraceId)) {
            apmGTraceId = invocation.getAttachment(Headers.GTRACE_ID.getValue());
        }
        StartTraceRequest startTraceRequest = new StartTraceRequest(className, methodName, apmTraceId, apmSpanId, apmGTraceId);
        startTraceRequest.setKind(AliDubboServiceImpl.PROVIDER_TAG);
        startTraceRequest.setSource(source);
        startTraceRequest.setRealSource(source);
        apmSpanEvent = TraceCollector.onStart(startTraceRequest);
        if (apmSpanEvent != null) {
            apmSpanEvent.addTag("url", source);
        }
        return apmSpanEvent;
    }

    private void transformAccess(Invoker invoker, Invocation invocation, String serviceUniqueName, String methodName,
                                 SpanEvent apmSpanEvent, String side, String protocol, String application) {
        TransformAccess transformAccess = (TransformAccess) invocation;
        FutureStatsAccessor futureStatsAccessor = new FutureStatsAccessor();
        transformAccess.setLopsAttribute(futureStatsAccessor);
        futureStatsAccessor.setServiceName(serviceUniqueName);
        futureStatsAccessor.setMethod(methodName);
        futureStatsAccessor.setStartTime(System.nanoTime());
        if (apmSpanEvent != null) {
            apmSpanEvent.addTag("serviceUniqueName", serviceUniqueName);
            apmSpanEvent.addTag("method", methodName);
            apmSpanEvent.addTag("side", side);
            apmSpanEvent.addTag("protocol", protocol);
            apmSpanEvent.addTag("application", application);
            if (RpcUtils.isAsync(invoker.getUrl(), invocation)) {
                apmSpanEvent.setAsync(true);
            } else {
                apmSpanEvent.setAsync(false);
            }
            if (RpcUtils.isOneway(invoker.getUrl(), invocation)) {
                apmSpanEvent.addTag("oneway", "true");
            } else {
                apmSpanEvent.addTag("oneway", "false");
            }
            futureStatsAccessor.setSpanEvent(apmSpanEvent);
        }
    }
}
