package io.sermant.monitor.util;

import io.sermant.core.utils.StringUtils;
import io.sermant.monitor.config.MonitorServiceConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * monitoring tool test class
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
