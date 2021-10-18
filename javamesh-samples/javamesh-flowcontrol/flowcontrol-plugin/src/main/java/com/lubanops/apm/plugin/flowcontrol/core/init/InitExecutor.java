/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.init;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.lubanops.apm.plugin.flowcontrol.core.config.CommonConst;
import com.lubanops.apm.plugin.flowcontrol.core.config.ConfigConst;
import com.lubanops.apm.plugin.flowcontrol.core.heartbeat.KafkaHeartbeatSender;
import com.lubanops.apm.plugin.flowcontrol.core.metric.SimpleKafkaMetricSender;
import com.lubanops.apm.plugin.flowcontrol.core.util.PluginConfigUtil;
import org.apache.kafka.common.KafkaException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 初始操作
 *
 * @author liyi
 * @since 2020-08-26
 */
public final class InitExecutor {
    private static final ScheduledExecutorService POOL = new ScheduledThreadPoolExecutor(2,
        new NamedThreadFactory(CommonConst.SENTINEL_SEND_CFC_TASK, true),
        new ThreadPoolExecutor.DiscardOldestPolicy());

    private InitExecutor() {
    }

    /**
     * 初始化定时器发送流控数据和心跳数据
     */
    public static void doInit() {
        // 周期性向kafka推送流控数据
        metricSenderInit();

        // 周期性向kafka推送心跳
        heartbeatSenderInit();
    }

    /**
     * 周期性向kafka推送流控数据
     */
    private static void metricSenderInit() {
        RecordLog.info("[InitExecutor] metricSenderInit() begin..");

        // 监控数据发送时间间隔
        long metricIntervalMs;
        try {
            String metricInterval = PluginConfigUtil.getValueByKey(ConfigConst.DEFAULT_METRIC_INTERVAL);

            // 加载配置参数
            metricIntervalMs = Long.parseLong(metricInterval);
        } catch (NumberFormatException e) {
            metricIntervalMs = CommonConst.SENTINEL_METRIC_INTERVAL;
            RecordLog.warn("[InitExecutor] metricSenderInit() config center", e.toString());
        }

        RecordLog.info("[InitExecutor] metricSenderInit() metricIntervalMs=" + metricIntervalMs);

        if (metricIntervalMs >= 0) {
            // 创建监控数据发送对象
            final SimpleKafkaMetricSender sender = new SimpleKafkaMetricSender();

            // 开启周期性执行线程
            POOL.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        sender.sendMetric();
                    } catch (KafkaException e) {
                        RecordLog.warn("[InitExecutor] Send metric error", e.toString());
                    }
                }
            }, CommonConst.INITIAL_DELAY, metricIntervalMs, TimeUnit.MILLISECONDS);
            RecordLog.info("[InitExecutor] metricSenderInit() end: " + sender.getClass().getCanonicalName());
        } else {
            RecordLog.error("[InitExecutor] metricIntervalMs is less than 0");
        }
    }

    /**
     * 周期性向kafka推送心跳
     */
    private static void heartbeatSenderInit() {
        RecordLog.info("[InitExecutor] heartbeatSenderInit() begin..");

        // 心跳数据发送时间间隔
        Long heartbeatIntervalMs = TransportConfig.getHeartbeatIntervalMs();

        // 先取启动参数，为空时才加载配置参数
        if (heartbeatIntervalMs == null) {
            heartbeatIntervalMs = Long.parseLong(PluginConfigUtil
                .getValueByKey(ConfigConst.DEFAULT_HEARTBEAT_INTERVAL));
        }

        if (heartbeatIntervalMs >= 0) {
            // 创建心跳数据发送对象
            final KafkaHeartbeatSender sender = new KafkaHeartbeatSender();
            RecordLog.info("[InitExecutor] heartbeatSenderInit() heartbeatIntervalMs=" + heartbeatIntervalMs);

            // 开启周周期性执行线程
            POOL.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        sender.sendHeartbeat();
                    } catch (Exception e) {
                        RecordLog.warn("[InitExecutor] heartbeatSenderInit() send heartbeat error", e);
                    }
                }
            }, CommonConst.INITIAL_DELAY, heartbeatIntervalMs, TimeUnit.MILLISECONDS);

            RecordLog.info("[InitExecutor] heartbeatSenderInit() end: "
                    + sender.getClass().getCanonicalName());
        } else {
            RecordLog.error("[InitExecutor] heartbeatIntervalMs is less than 0");
        }
    }
}
