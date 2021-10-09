/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import com.lubanops.apm.bootstrap.collector.api.FutureStatsAccessor;
import com.lubanops.apm.bootstrap.trace.Headers;
import com.lubanops.apm.bootstrap.trace.SpanEvent;
import com.lubanops.apm.bootstrap.trace.StartTraceRequest;
import com.lubanops.apm.bootstrap.trace.TraceCollector;
import com.lubanops.apm.bootstrap.utils.StringUtils;
import com.lubanops.apm.plugin.flowrecord.config.CommonConst;
import com.lubanops.apm.plugin.flowrecord.config.ConfigConst;
import com.lubanops.apm.plugin.flowrecord.config.CorrelationConst;
import com.lubanops.apm.plugin.flowrecord.domain.RecordContext;
import com.lubanops.apm.plugin.flowrecord.domain.RecordJob;
import com.lubanops.apm.plugin.flowrecord.domain.RecordStatus;
import com.lubanops.apm.plugin.flowrecord.domain.Recorder;
import com.lubanops.apm.plugin.flowrecord.utils.AppNameUtil;
import com.lubanops.apm.plugin.flowrecord.utils.DubboUtil;
import com.lubanops.apm.plugin.flowrecord.utils.KafkaProducerUtil;
import com.lubanops.apm.plugin.flowrecord.utils.PluginConfigUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.dubbo.common.constants.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.curator.shaded.com.google.common.hash.Hashing;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;


import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * alibaba dubbo拦截后的增强类
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */
public class AliDubboInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AliDubboInterceptor.class);
    public static final String CONSUMER_TAG = "DUBBO_CONSUMER";
    public static final String PROVIDER_TAG = "DUBBO_PROVIDER";

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
            start = isRecord(RecordJob.recordJob, invocation);
        } catch (Exception e) {
            start = false;
        }
        CorrelationConst.VALUE_MAX_LENGTH = 1280;
        CorrelationConst.ELEMENT_MAX_NUMBER = 10;

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
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        Invoker invoker = (Invoker) arguments[0];
        Invocation invocation = (Invocation) arguments[1];
        boolean start;

        if (RecordJob.recordJob != null) {
            start = isRecord(RecordJob.recordJob, invocation);
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
                LOGGER.error("sendRecorder error: {}", throwable.getMessage());
            }

        }
        TraceCollector.onFinally();

        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }

    private String generateOperationName(URL requesturl, Invocation invocation) {
        StringBuilder operationName = new StringBuilder();
        operationName.append(requesturl.getPath());
        operationName.append("." + invocation.getMethodName() + "(");
        for (Class<?> classes : invocation.getParameterTypes()) {
            operationName.append(classes.getSimpleName() + ",");
        }

        if (invocation.getParameterTypes().length > 0) {
            operationName.delete(operationName.length() - 1, operationName.length());
        }

        operationName.append(")");

        return operationName.toString();
    }

    /**
     * Format request url. e.g. dubbo://127.0.0.1:20880/org.apache.skywalking.apm.plugin.test.Test.test(String).
     *
     * @return request url.
     */
    private String generateRequesturl(URL url, Invocation invocation) {
        StringBuilder requesturl = new StringBuilder();
        requesturl.append(url.getProtocol() + "://");
        requesturl.append(url.getHost());
        requesturl.append(CommonConst.COLON_SIGN + url.getPort() + CommonConst.SLASH_SIGN);
        requesturl.append(generateOperationName(url, invocation));
        return requesturl.toString();
    }

    private void sendRecorder(Invocation invocation, Result ret, boolean entry, Invoker invoker, String jobId,
                              RecordContext recordContext) throws Throwable {
        Recorder recordRequest = Recorder.builder().build();
        recordRequest.setTraceId(TraceCollector.getVirtualTraceId());
        recordRequest.setJobId(jobId);
        recordRequest.setMethodName(invoker.getInterface().getCanonicalName() + CommonConst.POINT_SIGN +
                invocation.getMethodName());
        recordRequest.setAppType(ConfigConst.DUBBO_APP_TYPE);
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
        KafkaProducerUtil.sendMessage(PluginConfigUtil.getValueByKey(ConfigConst.KAFKA_REQUEST_TOPIC), serializedrequest);
    }

    private boolean isRecord(RecordJob recordJob, Invocation invocation) throws UnknownHostException {
        if (!recordJob.isTrigger()) {
            return false;
        }
        if (!recordJob.getApplication().equals(AppNameUtil.getAppName())) {
            return false;
        }

        InetAddress addr = InetAddress.getLocalHost();
        String localaddr = addr.getHostAddress();

        if (!recordJob.getMachineList().contains(localaddr)) {
            return false;
        }

        if (!recordJob.getMethodList().isEmpty() && !recordJob.getMethodList().contains(invocation.getMethodName())) {
            return false;
        }

        Date date = new Date();
        if (recordJob.getStartTime().before(date) && recordJob.getEndTime().after(date)) {
            return true;
        } else {
            return false;
        }
    }

    private void correlationInsert(Invoker invoker, Invocation invocation) {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        if (relationContext == null || relationContext.get(ConfigConst.RECORD_LIST) == null) {
            RecordStatus.map.put(RecordJob.recordJob.getJobId(), invoker.getInterface().getName() + CommonConst.POINT_SIGN
                    + invocation.getMethodName());
            String map = JSON.toJSONString(RecordStatus.map);
            relationContext.put(ConfigConst.RECORD_LIST, map);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(relationContext.get(ConfigConst.RECORD_LIST));
            RecordStatus.map = (Map) jsonObject;
            RecordStatus.map.put(RecordJob.recordJob.getJobId(), invoker.getInterface().getName() + CommonConst.POINT_SIGN
                    + invocation.getMethodName());
            String map = JSON.toJSONString(RecordStatus.map);
            relationContext.put(ConfigConst.RECORD_LIST, map);
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
            apmSpanEvent = TraceCollector.onStart(className, methodName, AliDubboInterceptor.CONSUMER_TAG);
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
        if (StringUtils.isBlank(apmTraceId)) {
            apmGTraceId = invocation.getAttachment(Headers.GTRACE_ID.getValue());
        }
        StartTraceRequest startTraceRequest = new StartTraceRequest(className, methodName, apmTraceId, apmSpanId, apmGTraceId);
        startTraceRequest.setKind(AliDubboInterceptor.PROVIDER_TAG);
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