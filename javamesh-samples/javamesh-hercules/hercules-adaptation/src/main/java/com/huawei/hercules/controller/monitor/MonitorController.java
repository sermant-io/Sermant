/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor;

import com.huawei.hercules.controller.monitor.influxdb.SqlModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MonitorService monitorService;

    @GetMapping("/monitor")
    public MonitorModel getMonitorInfo(@RequestParam String ip, @RequestParam String host) {
        SqlModel sqlModel = new SqlModel();
        sqlModel.setIp(ip);
        Map<String, Object> allMonitorData = monitorService.getAllMonitorData(sqlModel);
        MonitorModel monitorModel = new MonitorModel();
        monitorModel.setSuccess(true);
        monitorModel.setData(allMonitorData);
        return monitorModel;
    }
}
