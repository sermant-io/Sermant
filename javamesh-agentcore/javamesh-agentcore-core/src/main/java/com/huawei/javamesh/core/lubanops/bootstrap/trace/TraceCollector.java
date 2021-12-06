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

package com.huawei.javamesh.core.lubanops.bootstrap.trace;

import com.huawei.javamesh.core.lubanops.bootstrap.agent.AgentInfo;
import com.huawei.javamesh.core.lubanops.bootstrap.api.APIService;
import com.huawei.javamesh.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.SpanEvent.DiscardInfo;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 调用链数据采集
 * @author
 */
public class TraceCollector {

    /**
     * 每级最大spanevent个数
     */
    private final static int MAX_SPAN_EVENT_COUNT = 500;

    public final static ThreadLocal<String> G_TRACE_ID_THREAD_LOCAL = new ThreadLocal<String>();

    public final static ThreadLocal<DiscardInfo> DISCARD_INFO = new ThreadLocal<DiscardInfo>();

    private static ThreadLocal<SpanEvent> threadLocal = new ThreadLocal<SpanEvent>();

    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * trace reporter internal
     */
    private static TraceReportService traceReportService;

    /**
     * 调用链根开始采集
     * @param startTraceRequest
     */
    public static SpanEvent onStart(StartTraceRequest startTraceRequest) {
        filterSpanId(startTraceRequest);
        // 判断采样率
        if (startTraceRequest.getSampleFilter() == null
                || hasTraceId(startTraceRequest)
                || startTraceRequest.getSampleFilter()
                        .sample(startTraceRequest.getSource(), startTraceRequest.getHttpMethod())) {
            long startTime = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            SpanEvent spanEvent = new SpanEvent(startTraceRequest.getTraceId(), startTraceRequest.getSpanId(),
                    startTraceRequest.getDomainId());
            spanEvent.setStartTime(startTime);
            spanEvent.setType(startTraceRequest.getKind());
            spanEvent.setThreadId(Thread.currentThread().getId());
            spanEvent.setClassName(startTraceRequest.getTraceClass());
            spanEvent.setMethod(startTraceRequest.getMethod());
            spanEvent.setSource(startTraceRequest.getSource());
            spanEvent.setRealSource(startTraceRequest.getRealSource());
            spanEvent.setStartNanoTime(nanoTime);
            spanEvent.setSourceEventId(startTraceRequest.getSourceEventId());
            spanEvent.setGlobalTraceId(setGtraceId(startTraceRequest, spanEvent));
            if (startTraceRequest.getHttpMethod() != null) {
                spanEvent.addTag("httpMethod", startTraceRequest.getHttpMethod());
            }
            threadLocal.set(spanEvent);
            return spanEvent;
        } else {
            setGtraceId(startTraceRequest, null);
        }
        return null;
    }

    public static void filterSpanId(StartTraceRequest startTraceRequest) {
        // 检查spanId长度 大于100忽略
        String spanId = startTraceRequest.getSpanId();
        if (spanId != null && spanId.length() > 100) {
            startTraceRequest.setTraceId(null);
            startTraceRequest.setSpanId(null);
        }
    }

    public static boolean hasTraceId(StartTraceRequest startTraceRequest) {
        if (isSameDomain(startTraceRequest) && !StringUtils.isBlank(startTraceRequest.getTraceId())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSameDomain(StartTraceRequest startTraceRequest) {
        String fromDomainId = startTraceRequest.getDomainId();
        String toDomainId = String.valueOf(IdentityConfigManager.getDomainId());
        if (toDomainId.equals(fromDomainId)) {
            return true;
        } else {
            return false;
        }
    }

    public static String setGtraceId(StartTraceRequest startTraceRequest, SpanEvent spanEvent) {
        String globalTraceId = null;
        if (!StringUtils.isBlank(startTraceRequest.getgTraceId()) && isSameDomain(startTraceRequest)) {
            globalTraceId = startTraceRequest.getgTraceId();
        } else {
            if (spanEvent == null) {
                globalTraceId = AgentInfo.generateVirtualTraceId();
            } else {
                globalTraceId = spanEvent.getTraceId();
            }
        }
        G_TRACE_ID_THREAD_LOCAL.set(globalTraceId);
        return globalTraceId;
    }

    /**
     * 调用链开始采集
     */
    public static SpanEvent onStart(String className, String method, String kind) {
        SpanEvent spanEvent = threadLocal.get();
        if (spanEvent != null) {
            long nanoTime = System.nanoTime();
            if (spanEvent.getChildSpanEventCount() < MAX_SPAN_EVENT_COUNT) {
                long startTime = System.currentTimeMillis();
                SpanEvent newSpanEvent = new SpanEvent(spanEvent);
                if (newSpanEvent.getEventId().length() > 500) {
                    LOGGER.log(Level.SEVERE,
                            "spanEventId is too long" + APIService.getJsonApi().toJSONString(spanEvent));
                    threadLocal.set(null);
                    return null;
                } else {
                    newSpanEvent.setStartTime(startTime);
                    newSpanEvent.setStartNanoTime(nanoTime);
                    newSpanEvent.setClassName(className);
                    newSpanEvent.setMethod(method);
                    newSpanEvent.setType(kind);
                    threadLocal.set(newSpanEvent);
                    return newSpanEvent;
                }
            } else {
                if (spanEvent.getDisableDeep() == 0) {
                    Map<String, DiscardInfo> map = spanEvent.getDiscardMap();
                    DiscardInfo discardInfo = map.get(kind);
                    if (discardInfo == null) {
                        discardInfo = new DiscardInfo();
                        map.put(kind, discardInfo);
                    }
                    discardInfo.setType(kind);
                    spanEvent.setDiscardSpanEventStartTime(nanoTime);
                    DISCARD_INFO.set(discardInfo);
                }
                spanEvent.addDisableDeep();
                return null;
            }
        }
        return spanEvent;
    }

    /**
     * 调用链异常采集
     * @param e
     */
    public static SpanEvent onError(Throwable e) {
        return onError(threadLocal.get(), e);
    }

    /**
     * 调用链异常采集
     * @param e
     */
    public static SpanEvent onError(SpanEvent spanEvent, Throwable e) {
        if (spanEvent != null && e != null) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            spanEvent.addTag("exceptionType", e.getClass().getName());
            spanEvent.addTag("exceptionMsg", e.getMessage(), 10000);
            spanEvent.setSpanError(true);
            spanEvent.setErrorReasons(ErrorType.EXCEPTION_ERR.name());
        }
        return spanEvent;
    }

    /**
     * 调用链结束采集
     */
    public static SpanEvent onFinally(boolean send) {
        SpanEvent spanEvent = threadLocal.get();
        if (spanEvent != null) {
            long endTime = System.nanoTime();
            if (spanEvent.getDisableDeep() == 0) {
                if (spanEvent.getParent() == null) {
                    return null;
                }
                long timeUsed = endTime - spanEvent.getStartNanoTime();
                spanEvent.setTimeUsed(timeUsed);
                threadLocal.set(spanEvent.getParent());
                spanEvent.setDiscardInfo();
                if (send) {
                    // 发送spanevent数据
                    sendSpanEvent(spanEvent);
                }
            } else {
                saveDiscardInfo(spanEvent, endTime);
                return null;
            }
        }
        return spanEvent;
    }

    private static void saveDiscardInfo(SpanEvent spanEvent, long endTime) {
        spanEvent.subDisableDeep();
        if (spanEvent.getDisableDeep() == 0) {
            DiscardInfo discardInfo = DISCARD_INFO.get();
            if (discardInfo != null) {
                DISCARD_INFO.set(null);
                if (spanEvent.getDiscardSpanEventStartTime() > 0) {
                    long timeUsed = endTime - spanEvent.getDiscardSpanEventStartTime();
                    spanEvent.setDiscardSpanEventStartTime(0);
                    discardInfo.setCount(discardInfo.getCount() + 1);
                    discardInfo.setTotalTime(discardInfo.getTotalTime() + timeUsed);
                }
            }

        }
    }

    /**
     * 调用链结束采集
     */
    public static SpanEvent onFinally() {
        return onFinally(true);
    }

    /**
     * 调用链根结束采集
     */
    public static SpanEvent onFinally(int code, boolean isError) {
        SpanEvent spanEvent = threadLocal.get();
        if (spanEvent != null) {
            long endTime = System.nanoTime();
            long timeUsed = endTime - spanEvent.getStartNanoTime();
            spanEvent.setTimeUsed(timeUsed);
            spanEvent.setCode(code);
            if (isError) {
                spanEvent.setSpanError(isError);
                spanEvent.setErrorReasons(ErrorType.CODE_ERR.name());
            }
            threadLocal.set(null);
            spanEvent.setDiscardInfo();
            spanEvent.setThreadId(null);
            // 发送spanevent数据
            sendSpanEvent(spanEvent);
        }
        G_TRACE_ID_THREAD_LOCAL.set(null);
        return spanEvent;
    }

    public static SpanEvent onRootFinallyNoSend() {
        SpanEvent spanEvent = threadLocal.get();
        if (spanEvent != null) {
            long endTime = System.nanoTime();
            long timeUsed = endTime - spanEvent.getStartNanoTime();
            spanEvent.setTimeUsed(timeUsed);
            threadLocal.set(null);
            spanEvent.setDiscardInfo();
        }
        G_TRACE_ID_THREAD_LOCAL.set(null);
        return spanEvent;
    }

    /**
     * 调用链根结束采集
     */
    public static SpanEvent onFinally(int code) {
        return onFinally(code, false);
    }

    /**
     * 发送spanevent
     */
    public static void sendSpanEvent(SpanEvent spanEvent) {
        if (spanEvent == null) {
            LOGGER.log(Level.SEVERE, "[Trace Collector]push span event error,NOP event.");
            return;
        }
        traceReportService.offerEvent(spanEvent);
    }

    public static void onFinallySpanEvent(SpanEvent spanEvent) {
        long endTime = System.nanoTime();
        long timeUsed = endTime - spanEvent.getStartNanoTime();
        spanEvent.setTimeUsed(timeUsed);
        traceReportService.offerEvent(spanEvent);
    }

    /**
     * 清空threadLocal中的spanevent
     */
    public static void clear() {
        threadLocal.set(null);
        G_TRACE_ID_THREAD_LOCAL.set(null);
    }

    /**
     * 获取当前spanEvent
     * @return
     */
    public static SpanEvent getSpanEvent() {
        SpanEvent spanevent = threadLocal.get();
        if (spanevent == null || spanevent.getDisableDeep() > 0) {
            return null;
        }
        return spanevent;
    }

    /**
     * 设置spanevent
     * @param spanEvent
     */
    public static void setSpanEvent(SpanEvent spanEvent) {
        threadLocal.set(spanEvent);
    }

    /**
     * 获取当前traceId
     * @return
     */
    public static String getTraceId() {
        SpanEvent spanEvent = TraceCollector.getSpanEvent();
        return spanEvent == null ? null : spanEvent.getTraceId();
    }

    /**
     * 获取v-traceId
     * @return
     */
    public static String getVirtualTraceId() {
        return G_TRACE_ID_THREAD_LOCAL.get();
    }

    /**
     * 添加参数
     * @param key
     * @param value
     */
    public static void addTag(String key, String value) {
        SpanEvent spanEvent = threadLocal.get();
        if (spanEvent != null && spanEvent.getDisableDeep() == 0) {
            spanEvent.addTag(key, value);
        }
    }

    /**
     * 添加参数限制长度
     * @param key
     * @param value
     */
    public static void addTag(String key, String value, int limit) {
        SpanEvent spanEvent = threadLocal.get();
        if (spanEvent != null && spanEvent.getDisableDeep() == 0) {
            spanEvent.addTag(key, value, limit);
        }
    }

    /**
     * 解析cse的请求传入的数据
     * @param cseContext
     * @return
     */
    public static Map<String, Object> getTraceMapByCseContext(String cseContext) {
        if (cseContext != null) {
            try {
                HashMap<String, Object> map = APIService.getJsonApi().parseObject(cseContext, HashMap.class);
                return map;
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static String getCseMapValue(Map map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public static void setReportService(TraceReportService traceReportService) {
        TraceCollector.traceReportService = traceReportService;
    }
}
