package com.huawei.monitor.util;

import com.huawei.monitor.config.MonitorServiceConfig;

import com.huaweicloud.sermant.core.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 监控工具测试类
 *
 * @author zhp
 * @since 2022-11-08
 */
public class MonitorUtilTest {
    @Test
    public void startMonitor() {
        MonitorServiceConfig metricConfig = new MonitorServiceConfig();
        Assert.assertFalse(metricConfig != null && metricConfig.isEnableStartService()
                && StringUtils.isNoneBlank(metricConfig.getReportType()));
        metricConfig.setEnableStartService(true);
        Assert.assertFalse(metricConfig != null && metricConfig.isEnableStartService()
                && StringUtils.isNoneBlank(metricConfig.getReportType()));
        metricConfig.setReportType("PROMETHEUS");
        Assert.assertTrue(metricConfig != null && metricConfig.isEnableStartService()
                && StringUtils.isNoneBlank(metricConfig.getReportType()));
    }
}