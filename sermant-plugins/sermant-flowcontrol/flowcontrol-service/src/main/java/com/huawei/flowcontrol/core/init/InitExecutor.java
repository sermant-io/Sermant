/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.init;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.enums.MetricSendWay;
import com.huawei.flowcontrol.common.util.PluginConfigUtil;
import com.huawei.flowcontrol.core.metric.provider.DefaultMetricProvider;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 初始操作
 *
 * @author zhouss
 * @since 2022-02-11
 */
public final class InitExecutor {
    private static final ScheduledExecutorService POOL = new ScheduledThreadPoolExecutor(1,
        new NamedThreadFactory(CommonConst.SENTINEL_SEND_CFC_TASK, true),
        new ThreadPoolExecutor.DiscardOldestPolicy());

    private static FlowControlConfig pluginConfig;

    private InitExecutor() {
    }

    /**
     * 初始化定时器发送流控数据和心跳数据
     */
    public static void doInit() {
        pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (!pluginConfig.isOpenMetricCollector()) {
            return;
        }

        // 周期性向kafka推送流控数据
        metricSenderInit();
    }

    /**
     * 释放线程池
     */
    public static void stop() {
        POOL.shutdown();
    }

    /**
     * 周期性向kafka推送流控数据
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private static void metricSenderInit() {
        RecordLog.info("[InitExecutor] metricSenderInit() begin..");

        // 监控数据发送时间间隔
        long metricIntervalMs;
        try {
            String metricInterval = PluginConfigUtil.getValueByKey(ConfigConst.DEFAULT_METRIC_INTERVAL);

            // 加载配置参数
            metricIntervalMs = Long.parseLong(metricInterval);
        } catch (NumberFormatException e) {
            metricIntervalMs = CommonConst.FLOW_CONTROL_METRIC_INTERVAL;
            RecordLog.warn("[InitExecutor] metricSenderInit() config center", e.toString());
        }

        RecordLog.info("[InitExecutor] metricSenderInit() metricIntervalMs=" + metricIntervalMs);
        if (metricIntervalMs >= 0) {
            // 创建监控数据发送对象
            final MetricSendWay sendWay = pluginConfig.getSendWay();
            final DefaultMetricProvider defaultMetricProvider = new DefaultMetricProvider();

            // 开启周期性执行线程
            POOL.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendWay.getSender().sendMetric(defaultMetricProvider);
                    } catch (Exception e) {
                        RecordLog.warn("[InitExecutor] Send metric error", e.toString());
                    }
                }
            }, CommonConst.INITIAL_DELAY, metricIntervalMs, TimeUnit.MILLISECONDS);
            RecordLog
                .info("[InitExecutor] metricSenderInit() end: " + sendWay.getSender().getClass().getCanonicalName());
        } else {
            RecordLog.error("[InitExecutor] metricIntervalMs is less than 0");
        }
    }
}
