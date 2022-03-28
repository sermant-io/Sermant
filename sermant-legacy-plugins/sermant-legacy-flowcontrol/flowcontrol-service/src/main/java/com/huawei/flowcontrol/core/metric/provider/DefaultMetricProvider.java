/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.core.metric.provider;

import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.metric.provider.MetricProvider;
import com.huawei.flowcontrol.common.util.PluginConfigUtil;
import com.huawei.sermant.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 默认指标数据生成 基于sentinel本地数据
 *
 * @author zhouss
 * @since 2021-12-07
 */
public final class DefaultMetricProvider implements MetricProvider {
    private final MetricSearcher metricSearcher;

    private FlowControlConfig flowControlConfig;

    /**
     * sentinel内部定义的指标数据类型，该类型不应该展示在界面上，该类数据全部跳过
     */
    private final Set<String> innerResources = new HashSet<String>(
        Arrays.asList(Constants.CPU_USAGE_RESOURCE_NAME,
            Constants.TOTAL_IN_RESOURCE_NAME,
            Constants.SYSTEM_LOAD_RESOURCE_NAME));

    public DefaultMetricProvider() {
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = IdentityConfigManager.getAppName();
        }
        metricSearcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
            MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
    }

    private FlowControlConfig getFlowControlConfig() {
        if (flowControlConfig == null) {
            flowControlConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        }
        return flowControlConfig;
    }

    /**
     * 获取流控数据
     *
     * @param startTime 查询流控数据的开始时间
     * @param endTime 查询流控数据的结束书简
     * @return 流控数据
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private String generateCurrentMessage(long startTime, long endTime) {
        String message = null;
        List<MetricNode> metrics;

        try {
            metrics = metricSearcher.find(startTime,
                Integer.parseInt(PluginConfigUtil.getValueByKey(ConfigConst.METRIC_MAX_LINE)));
            if (metrics != null && metrics.size() > 0) {
                message = formatMessage(metrics);
            }
        } catch (Exception e) {
            RecordLog.error("[MetricMessage] Find metric is failed." + e);
        }
        return message;
    }

    /**
     * 格式化metric对象list集合
     *
     * @param list metric消息的list集合
     * @return 返回对象集合字符串
     */
    private String formatMessage(List<MetricNode> list) {
        final List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
        for (MetricNode node : list) {
            if (isInnerMetric(node)) {
                continue;
            }
            metricEntities.add(new MetricEntity()
                .setResource(node.getResource())
                .setTimestamp(node.getTimestamp())
                .setBlockQps(node.getBlockQps())
                .setPassQps(node.getPassQps())
                .setSuccessQps(node.getSuccessQps())
                .setExceptionQps(node.getExceptionQps())
                .setClassification(node.getClassification())
                .setRt(node.getRt())
                .setResourceCode(node.getResource().hashCode())
                .setApp(AppNameUtil.getAppName()));
        }
        return JSONArray.toJSONString(metricEntities);
    }

    @Override
    public String buildMetric() {
        final long metricSleepTimeMs = getFlowControlConfig().getMetricSleepTimeMs();
        long endTime = TimeUtil.currentTimeMillis();
        long startTime = endTime - metricSleepTimeMs;
        try {
            Thread.sleep(metricSleepTimeMs);
        } catch (InterruptedException ignore) {
            // ignored
        }
        return generateCurrentMessage(startTime, endTime);
    }

    private boolean isInnerMetric(MetricNode node) {
        return innerResources.contains(node.getResource());
    }
}
