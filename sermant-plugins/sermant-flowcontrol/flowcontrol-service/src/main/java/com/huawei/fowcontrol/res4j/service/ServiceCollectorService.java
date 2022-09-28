/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.huawei.fowcontrol.res4j.service;

import com.huawei.flowcontrol.common.entity.MetricCalEntity;
import com.huawei.flowcontrol.common.entity.MetricEntity;
import com.huawei.flowcontrol.common.enums.MetricType;
import com.huawei.fowcontrol.res4j.chain.handler.MonitorHandler;
import com.huawei.fowcontrol.res4j.util.MonitorUtils;

import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.utils.StringUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务指标监控
 *
 * @author zhp
 * @since 2022-08-30
 */
public class ServiceCollectorService extends Collector implements PluginService {
    /**
     * 熔断器集合，用于获取熔断器指标信息
     */
    public static final Map<String, CircuitBreaker> CIRCUIT_BREAKER_MAP = new ConcurrentHashMap<>();

    private static Map<String, MetricEntity> monitors;

    private static final List<String> DEFAULT_LABEL_NAME = Collections.singletonList("name");

    private static Map<String, MetricEntity> lastMetricMap = new ConcurrentHashMap<>();

    private static Long lastStartTime;

    private static final int PROPORTION = 1000;

    @Override
    public void start() {
        if (!MonitorUtils.isStartMonitor()) {
            return;
        }
        monitors = MonitorHandler.MONITORS;
        this.register();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        Map<String, GaugeMetricFamily> metricMap = new HashMap<>();
        collectCircuitBreakerMetric(metricMap);
        List<MetricFamilySamples> samples = new ArrayList<>();
        if (monitors == null || monitors.isEmpty()) {
            buildDefaultMetric(samples);
            samples.addAll(metricMap.values());
            return samples;
        }
        Map<String, MetricEntity> currentMap = getCurrentMap(monitors);
        currentMap.forEach((k, v) -> {
            if (v == null || StringUtils.isBlank(v.getName())) {
                return;
            }
            collectFuseMetric(metricMap, k, v);
        });
        long currentTime = System.currentTimeMillis();
        collectThroughPutMetric(samples, currentTime, currentMap);
        lastStartTime = currentTime;
        lastMetricMap = currentMap;
        samples.addAll(metricMap.values());
        return samples;
    }

    /**
     * 采集吞吐量指标信息
     *
     * @param samples     指标集
     * @param currentTime 当前时间
     * @param currentMap  当前指标信息
     */
    private void collectThroughPutMetric(List<MetricFamilySamples> samples, long currentTime, Map<String,
            MetricEntity> currentMap) {
        MetricCalEntity metricCalEntity = fillMetricInfo(currentMap);
        MetricCalEntity lastCalEntity = fillMetricInfo(lastMetricMap);
        double consumeTime = metricCalEntity.getConsumeServerReqTimeSum() - lastCalEntity.getConsumeServerReqTimeSum();
        double serverRequestNum = metricCalEntity.getServerReqSum() - lastCalEntity.getServerReqSum();
        double failedServerRequestNum = metricCalEntity.getFailedServerReqSum() - lastCalEntity.getFailedServerReqSum();
        double serverFinishRequestNum = metricCalEntity.getSuccessFulServerReqSum()
                - lastCalEntity.getSuccessFulServerReqSum() + failedServerRequestNum;
        if (lastStartTime == null || serverRequestNum == 0L || consumeTime == 0) {
            buildDefaultMetric(samples);
        } else {
            long interval = currentTime - lastStartTime;
            double qps = serverFinishRequestNum * PROPORTION / interval;
            double responseTime = consumeTime / serverFinishRequestNum;
            double tps = qps * PROPORTION / responseTime;
            samples.add(new GaugeMetricFamily(MetricType.QPS.getName(), MetricType.QPS.getDesc(), qps));
            samples.add(new GaugeMetricFamily(MetricType.TPS.getName(), MetricType.TPS.getDesc(), tps));
            samples.add(new GaugeMetricFamily(MetricType.AVG_RESPONSE_TIME.getName(),
                    MetricType.AVG_RESPONSE_TIME.getDesc(), responseTime));
        }
    }

    /**
     * 构造默认返回信息
     *
     * @param samples 指标实例
     */
    private static void buildDefaultMetric(List<MetricFamilySamples> samples) {
        samples.add(new GaugeMetricFamily(MetricType.QPS.getName(), MetricType.QPS.getDesc(), 0));
        samples.add(new GaugeMetricFamily(MetricType.TPS.getName(), MetricType.TPS.getDesc(), 0));
        samples.add(new GaugeMetricFamily(MetricType.AVG_RESPONSE_TIME.getName(),
                MetricType.AVG_RESPONSE_TIME.getDesc(), 0));
    }

    /**
     * 采集熔断指标
     *
     * @param metricMap 指标采集Map
     * @param k         指标标签值
     * @param v         指标信息
     */
    private void collectFuseMetric(Map<String, GaugeMetricFamily> metricMap, String k, MetricEntity v) {
        MetricEntity lastMetric;
        if (lastMetricMap.get(k) == null) {
            lastMetric = new MetricEntity();
        } else {
            lastMetric = lastMetricMap.get(k);
        }
        long total = v.getFuseRequest().get() - lastMetric.getFuseRequest().get();
        addMetric(metricMap, MetricType.FUSED_REQUEST, total, k);
        long failure = v.getFailedClientRequest().get() - lastMetric.getFailedClientRequest().get();
        long ignore = v.getIgnoreFulFuseRequest().get() - lastMetric.getIgnoreFulFuseRequest().get();
        addMetric(metricMap, MetricType.FAILURE_FUSE_REQUEST, failure + ignore, k);
        double failRate = total == 0 ? 0 : (failure + ignore) / (double) total;
        addMetric(metricMap, MetricType.FAILURE_RATE_FUSE_REQUEST, failRate, k);
        if (lastStartTime == null) {
            addMetric(metricMap, MetricType.QPS, 0, k);
        } else {
            long interval = System.currentTimeMillis() - lastStartTime;
            double qps = interval == 0 ? 0 : total * PROPORTION / (double) interval;
            addMetric(metricMap, MetricType.QPS, qps, k);
        }
        long fuseTime = v.getFuseTime().get() - lastMetric.getFuseTime().get();
        double avgResponseTime = total == 0 ? 0 : fuseTime / (double) total;
        addMetric(metricMap, MetricType.AVG_RESPONSE_TIME, avgResponseTime, k);
        long slowNum = v.getSlowFuseRequest().get() - lastMetric.getSlowFuseRequest().get();
        addMetric(metricMap, MetricType.SLOW_CALL_NUMBER, slowNum, k);
        long permitted = v.getPermittedFulFuseRequest().get() - lastMetric.getPermittedFulFuseRequest().get();
        addMetric(metricMap, MetricType.PERMITTED_FUSE_REQUEST, permitted, k);
    }

    /**
     * 填充指标计算信息
     *
     * @param metricEntityMap 指标信息集
     * @return 指标计算信息
     */
    private MetricCalEntity fillMetricInfo(Map<String, MetricEntity> metricEntityMap) {
        MetricCalEntity metricCalEntity = new MetricCalEntity();
        if (metricEntityMap == null || metricEntityMap.isEmpty()) {
            return metricCalEntity;
        }
        metricEntityMap.forEach((k, v) -> {
            if (v == null) {
                return;
            }
            metricCalEntity.setServerReqSum(metricCalEntity.getServerReqSum() + v.getServerRequest().get());
            metricCalEntity.setSuccessFulServerReqSum(metricCalEntity.getSuccessFulServerReqSum()
                    + v.getSuccessServerRequest().get());
            metricCalEntity.setFailedServerReqSum(metricCalEntity.getFailedServerReqSum()
                    + v.getFailedServerRequest().get());
            metricCalEntity.setConsumeServerReqTimeSum(metricCalEntity.getConsumeServerReqTimeSum()
                    + v.getConsumeServerTime().get());
        });
        return metricCalEntity;
    }

    /**
     * 采集熔断器指标
     *
     * @param metricMap 指标Map
     */
    private void collectCircuitBreakerMetric(Map<String, GaugeMetricFamily> metricMap) {
        if (!CIRCUIT_BREAKER_MAP.isEmpty()) {
            CIRCUIT_BREAKER_MAP.forEach((key, circuitBreaker) -> {
                if (circuitBreaker == null) {
                    return;
                }
                CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
                addMetric(metricMap, MetricType.FAILURE_RATE, metrics.getFailureRate(), key);
                addMetric(metricMap, MetricType.SLOW_CALL_RATE, metrics.getSlowCallRate(), key);
                addMetric(metricMap, MetricType.SLOW_CALL_NUMBER, metrics.getNumberOfSlowCalls(), key);
                addMetric(metricMap, MetricType.BUFFERED_CALLS_NUMBER, metrics.getNumberOfBufferedCalls(), key);
                addMetric(metricMap, MetricType.FAILED_CALLS_NUMBER, metrics.getNumberOfFailedCalls(), key);
                addMetric(metricMap, MetricType.SLOW_CALL_FAILURE_NUMBER, metrics.getNumberOfSlowFailedCalls(), key);
                addMetric(metricMap, MetricType.SUCCESSFUL_CALLS_NUMBER, metrics.getNumberOfSuccessfulCalls(), key);
                addMetric(metricMap, MetricType.NOT_PERMITTED_CALLS_NUMBER, metrics.getNumberOfNotPermittedCalls(),
                        key);
                addMetric(metricMap, MetricType.SLOW_CALL_SUCCESS_NUMBER, metrics.getNumberOfSlowSuccessfulCalls(),
                        key);
            });
        }
    }

    /**
     * 构造指标收集器
     *
     * @return 收集器
     */
    private GaugeMetricFamily buildGaugeMetric(MetricType metricType) {
        return new GaugeMetricFamily(metricType.getName(), metricType.getDesc(),
                ServiceCollectorService.DEFAULT_LABEL_NAME);
    }

    /**
     * 增加指标信息
     *
     * @param metricMap  指标信息存储MAP
     * @param type       指标类型
     * @param value      指标值
     * @param labelValue 指标标签值
     */
    private void addMetric(Map<String, GaugeMetricFamily> metricMap, MetricType type, double value, String labelValue) {
        GaugeMetricFamily metric = metricMap.computeIfAbsent(type.getName(), s -> buildGaugeMetric(type));
        metric.addMetric(Collections.singletonList(labelValue), value);
    }

    /**
     * 暂存当前数据，防止数据在后续收集指标过程中被更新
     *
     * @param source 数据来源
     */
    private Map<String, MetricEntity> getCurrentMap(Map<String, MetricEntity> source) {
        Map<String, MetricEntity> target = new ConcurrentHashMap<>();
        if (source != null && !source.isEmpty()) {
            for (Map.Entry<String, MetricEntity> entry : source.entrySet()) {
                MetricEntity metricEntity = new MetricEntity();
                MetricEntity sourceMetric = entry.getValue();
                copy(metricEntity, sourceMetric);
                target.put(entry.getKey(), metricEntity);
            }
        }
        return target;
    }

    /**
     * 数据copy
     *
     * @param metricEntity 待拷贝数据
     * @param sourceMetric 元数据
     */
    private void copy(MetricEntity metricEntity, MetricEntity sourceMetric) {
        if (sourceMetric == null) {
            return;
        }
        metricEntity.setName(sourceMetric.getName());
        copyValue(sourceMetric.getServerRequest(), metricEntity.getServerRequest());
        copyValue(sourceMetric.getClientRequest(), metricEntity.getClientRequest());
        copyValue(sourceMetric.getConsumeClientTime(), metricEntity.getConsumeClientTime());
        copyValue(sourceMetric.getConsumeServerTime(), metricEntity.getConsumeServerTime());
        copyValue(sourceMetric.getSuccessServerRequest(), metricEntity.getSuccessServerRequest());
        copyValue(sourceMetric.getSuccessClientRequest(), metricEntity.getSuccessClientRequest());
        copyValue(sourceMetric.getFailedServerRequest(), metricEntity.getFailedServerRequest());
        copyValue(sourceMetric.getFailedClientRequest(), metricEntity.getFailedClientRequest());
        copyValue(sourceMetric.getLastTime(), metricEntity.getLastTime());
        copyValue(sourceMetric.getFuseTime(), metricEntity.getFuseTime());
        copyValue(sourceMetric.getFailedFuseRequest(), metricEntity.getFailedFuseRequest());
        copyValue(sourceMetric.getSuccessFulFuseRequest(), metricEntity.getSuccessFulFuseRequest());
        copyValue(sourceMetric.getPermittedFulFuseRequest(), metricEntity.getPermittedFulFuseRequest());
        copyValue(sourceMetric.getIgnoreFulFuseRequest(), metricEntity.getIgnoreFulFuseRequest());
        copyValue(sourceMetric.getSlowFuseRequest(), metricEntity.getSlowFuseRequest());
        copyValue(sourceMetric.getFuseRequest(), metricEntity.getFuseRequest());
        metricEntity.setReportTime(sourceMetric.getReportTime());
    }

    /**
     * 值拷贝
     *
     * @param source 来源
     * @param target 赋值的对象
     */
    private void copyValue(AtomicLong source, AtomicLong target) {
        if (source != null) {
            target.set(source.get());
        }
    }
}
