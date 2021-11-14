/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report;

import com.huawei.apm.core.config.BaseConfig;
import com.huawei.apm.core.config.ConfigTypeKey;

/**
 * 上报插件配置
 *
 * @author zhouss
 * @since 2021-11-02
 */
@ConfigTypeKey("route.reporter.plugin")
public class ReporterConfig implements BaseConfig {
    /**
     * 路由server地址
     */
    private String serverUrls = "http://localhost:8090";

    /**
     * 上报间隔，默认10s上报一次
     */
    private int reportIntervalMs = 10 * 1000;

    public String getServerUrls() {
        return serverUrls;
    }

    public void setServerUrls(String serverUrls) {
        this.serverUrls = serverUrls;
    }

    public int getReportIntervalMs() {
        return reportIntervalMs;
    }

    public void setReportIntervalMs(int reportIntervalMs) {
        this.reportIntervalMs = reportIntervalMs;
    }
}
