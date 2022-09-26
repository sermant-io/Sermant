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

package com.huawei.fowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.entity.MetricEntity;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.common.enums.MetricType;
import com.huawei.fowcontrol.res4j.chain.AbstractChainHandler;
import com.huawei.fowcontrol.res4j.chain.HandlerConstants;
import com.huawei.fowcontrol.res4j.chain.context.RequestContext;
import com.huawei.fowcontrol.res4j.util.MonitorUtils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import io.prometheus.client.Summary;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控处理器
 *
 * @author zhp
 * @since 2022-09-05
 */
public class MonitorHandler extends AbstractChainHandler {
    /**
     * 监控指标存储MAP
     */
    public static final Map<String, MetricEntity> MONITORS = new ConcurrentHashMap<>();

    private static final String START_TIME = "__MONITOR_BUSINESS_START_TIME__";

    private static final double[] QUANTILES = {0.5, 0.75, 0.9, 0.99};

    private static final double[] ERRORS = {0.05, 0.025, 0.01, 0.001};

    private static final int FIRST_PERCENTILE_INDEX = 0;

    private static final int SECOND_PERCENTILE_INDEX = 1;

    private static final int THREE_PERCENTILE_INDEX = 2;

    private static final int THIRD_PERCENTILE_INDEX = 3;

    private static final Summary.Builder BUILDER = Summary.build().labelNames("name", "requestType", "status")
            .quantile(QUANTILES[FIRST_PERCENTILE_INDEX], ERRORS[FIRST_PERCENTILE_INDEX])
            .quantile(QUANTILES[SECOND_PERCENTILE_INDEX], ERRORS[SECOND_PERCENTILE_INDEX])
            .quantile(QUANTILES[THREE_PERCENTILE_INDEX], ERRORS[THREE_PERCENTILE_INDEX])
            .quantile(QUANTILES[THIRD_PERCENTILE_INDEX], ERRORS[THIRD_PERCENTILE_INDEX])
            .name(MetricType.REQUEST.getName()).help(MetricType.REQUEST.getDesc());

    private static Summary requestLatency;

    static {
        if (MonitorUtils.isStartMonitor()) {
            requestLatency = BUILDER.register();
        }
    }

    @Override
    public void onBefore(RequestContext context, Set<String> businessNames) {
        context.save(START_TIME, System.currentTimeMillis());
        if (context.getRequestEntity() != null && !StringUtils.isEmpty(context.getRequestEntity().getApiPath())) {
            String name = context.getRequestEntity().getApiPath();
            MetricEntity metricEntity = MONITORS.computeIfAbsent(name, s -> new MetricEntity());
            if (context.getRequestEntity().getRequestType() == RequestEntity.RequestType.CLIENT) {
                metricEntity.getClientRequest().getAndIncrement();
            } else {
                metricEntity.getServerRequest().getAndIncrement();
            }
        }
        super.onBefore(context, businessNames);
    }

    @Override
    public void onThrow(RequestContext context, Set<String> businessNames, Throwable throwable) {
        super.onThrow(context, businessNames, throwable);
        if (context.getRequestEntity() != null && !StringUtils.isEmpty(context.getRequestEntity().getApiPath())) {
            String name = context.getRequestEntity().getApiPath();
            MetricEntity metricEntity = MONITORS.computeIfAbsent(name, s -> new MetricEntity());
            if (context.getRequestEntity().getRequestType() == RequestEntity.RequestType.CLIENT) {
                metricEntity.getFailedClientRequest().getAndIncrement();
            } else {
                metricEntity.getFailedServerRequest().getAndIncrement();
            }
            if (requestLatency != null && context.get(START_TIME, Long.class) != null && !StringUtils.isEmpty(name)
                    && context.getRequestEntity().getRequestType() != null) {
                long consumeTime = System.currentTimeMillis() - context.get(START_TIME, Long.class);
                requestLatency.labels(name, context.getRequestEntity().getRequestType().name(),
                        Boolean.FALSE.toString()).observe(consumeTime);
            }
        }
        context.remove(START_TIME);
    }

    @Override
    public void onResult(RequestContext context, Set<String> businessNames, Object result) {
        super.onResult(context, businessNames, result);
        if (context.getRequestEntity() != null && !StringUtils.isEmpty(context.getRequestEntity().getApiPath())) {
            String name = context.getRequestEntity().getApiPath();
            long consumeTime = 0L;
            if (context.get(START_TIME, Long.class) != null) {
                consumeTime = System.currentTimeMillis() - context.get(START_TIME, Long.class);
            }
            MetricEntity metricEntity = MONITORS.computeIfAbsent(name, s -> new MetricEntity());
            if (context.getRequestEntity().getRequestType() == RequestEntity.RequestType.CLIENT) {
                metricEntity.getSuccessServerRequest().getAndIncrement();
                metricEntity.getConsumeClientTime().getAndAdd(consumeTime);
            } else {
                metricEntity.getSuccessClientRequest().getAndIncrement();
                metricEntity.getConsumeServerTime().getAndAdd(consumeTime);
            }
            if (requestLatency != null && consumeTime != 0 && context.getRequestEntity().getRequestType() != null) {
                requestLatency.labels(name, context.getRequestEntity().getRequestType().name(), Boolean.TRUE.toString())
                        .observe(consumeTime);
            }
        }
        context.remove(START_TIME);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.MONITOR_ORDER;
    }

    @Override
    protected boolean isSkip(RequestContext context, Set<String> businessNames) {
        return !MonitorUtils.isStartMonitor();
    }
}