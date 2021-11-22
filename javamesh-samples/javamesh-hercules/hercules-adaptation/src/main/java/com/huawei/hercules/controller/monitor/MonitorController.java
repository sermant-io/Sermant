/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.perftest.MonitorHostKey;
import com.huawei.hercules.controller.perftest.TaskInfoKey;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.influxdb.IMonitorService;
import com.huawei.hercules.service.influxdb.SqlParam;
import com.huawei.hercules.service.influxdb.metric.tree.impl.RootMetric;
import com.huawei.hercules.service.perftest.IPerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 功能描述：监控分析数据获取
 *
 * @author z30009938
 * @since 2021-11-12
 */
@RestController
@RequestMapping("/api")
public class MonitorController {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private IMonitorService monitorService;

    @Autowired
    private IPerfTestService perfTestService;

    @GetMapping("/monitor")
    public MonitorModel getMonitorInfo(@RequestParam SqlParam sqlParam) {
        LOGGER.debug("Monitor param:{}", sqlParam);
        MonitorModel monitorModel = new MonitorModel();
        JSONObject perfTestInfo = perfTestService.getOne(sqlParam.getTestId());
        JSONArray monitorHosts = perfTestInfo.getJSONArray(TaskInfoKey.MONITORING_HOST.getServerKey());
        if (monitorHosts == null || monitorHosts.isEmpty()) {
            monitorModel.setSuccess(false);
            monitorModel.setData(null);
            return monitorModel;
        }

        RootMetric allMonitorData = monitorService.getAllMonitorData(sqlParam);
        monitorModel.setSuccess(true);
        monitorModel.setData(allMonitorData);
        return monitorModel;
    }

    /**
     * 查询需要监控的主机是否需要获取jvm数据
     *
     * @param sqlParam     查询传入的参数信息
     * @param monitorHosts 系统查询到的主机信息
     */
    private void initJvmConfig(SqlParam sqlParam, JSONArray monitorHosts) {
        if (sqlParam == null || StringUtils.isEmpty(sqlParam.getHost()) || StringUtils.isEmpty(sqlParam.getIp())) {
            throw new HerculesException("Host or ip can not be empty for monitoring.");
        }
        if (monitorHosts == null || monitorHosts.isEmpty()) {
            throw new HerculesException("Can not found this host in config of this test.");
        }
        for (int i = 0; i < monitorHosts.size(); i++) {
            JSONObject monitorHost = monitorHosts.getJSONObject(i);
            if (!sqlParam.getHost().equals(monitorHost.getString(MonitorHostKey.HOST.getServerKey()))) {
                continue;
            }
            if (!sqlParam.getIp().equals(monitorHost.getString(MonitorHostKey.IP.getServerKey()))) {
                continue;
            }
            Boolean isMonitorJvm = monitorHost.getBoolean(MonitorHostKey.IS_MONITOR_JVM.getServerKey());
            String jvmType = monitorHost.getString(MonitorHostKey.JVM_TYPE.getServerKey());
            sqlParam.setJvmType(jvmType);
            sqlParam.setMonitorJvm(isMonitorJvm);
            break;
        }
    }
}
