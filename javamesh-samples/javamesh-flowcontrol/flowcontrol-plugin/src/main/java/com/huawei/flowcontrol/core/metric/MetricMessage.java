/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.metric;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.fastjson.JSONArray;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流控信息数据
 *
 * @author liyi
 * @since 2020-08-26
 */
public final class MetricMessage {
    private static final MetricSearcher METRIC_SEARCHER;

    static {
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = "";
        }
        METRIC_SEARCHER = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
            MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
    }

    /**
     * 获取流控数据
     *
     * @param startTime 查询流控数据的开始时间
     * @param endTime   查询流控数据的结束书简
     * @return 流控数据
     */
    public static String generateCurrentMessage(long startTime, Long endTime) {
        String message = null;
        List<MetricNode> list;

        try {
            if (endTime != null) {
                list = METRIC_SEARCHER.findByTimeAndResource(startTime, endTime, null);
            } else {
                list = METRIC_SEARCHER.find(startTime,
                    Integer.parseInt(PluginConfigUtil.getValueByKey(ConfigConst.METRIC_MAXLINE)));
            }
            if (list != null && list.size() > 0) {
                RecordLog.info("[MetricMessage] list size=" + list.size());
                message = formatMessage(list);
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
    public static String formatMessage(List<MetricNode> list) {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for (MetricNode node : list) {
            map = new HashMap<String, Object>();

            // 资源名称
            map.put("resource", node.getResource());

            // 统计时间
            map.put("timestamp", node.getTimestamp());

            // 通过的qps
            map.put("passQps", node.getPassQps());

            // 限流的qps
            map.put("blockQps", node.getBlockQps());

            // 成功的qps
            map.put("successQps", node.getSuccessQps());

            // 发送异常的次数
            map.put("exceptionQps", node.getExceptionQps());

            // RT 平均响应时长
            map.put("rt", node.getRt());

            // 计数
            map.put("count", 1);

            // 资源的hashcode
            map.put("resourceCode", node.getResource().hashCode());

            // 应用名
            map.put("app", AppNameUtil.getAppName());

            // 分类
            map.put("classification", node.getClassification());

            // 并发性
            map.put("concurrency", node.getConcurrency());
            map.put("occupiedPassQps", node.getOccupiedPassQps());
            resultList.add(map);
        }
        return JSONArray.toJSONString(resultList);
    }
}
