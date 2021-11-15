package com.huawei.apm.core.lubanops.core.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.huawei.apm.core.lubanops.bootstrap.collector.CollectorManager;
import com.huawei.apm.core.lubanops.bootstrap.collector.MonitorItem;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.Collector;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.MetricSet;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.plugin.apm.APMCollector;
import com.huawei.apm.core.lubanops.core.utils.ReportDataBuilder;

import com.huawei.apm.core.lubanops.integration.access.inbound.MonitorDataBody;

/**
 * 收割数据任务
 * @author
 */
public class HarvestTask implements Runnable {

    private final static Logger LOG = LogFactory.getLogger();

    private ScheduledFuture<?> scheduledFuture;

    private volatile List<MonitorItem> monitorConfigList = new ArrayList<MonitorItem>();

    @Inject
    private MonitorReportService monitorReportService;

    @Override
    public void run() {
        try {
            harvest(this.monitorConfigList);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "failed to harvest:", e);
        }
    }

    public void harvest(List<MonitorItem> collectorList) {
        if (collectorList == null || collectorList.isEmpty()) {
            return;
        }

        long harvestTime = System.currentTimeMillis();
        for (MonitorItem monitorItemApp : collectorList) {
            try {
                String collectorName = monitorItemApp.getCollectorName();
                if (collectorName == null) {
                    continue;
                }
                Collector cb = CollectorManager.getCollector(collectorName);
                if (cb != null) {
                    MonitorDataBody body = new MonitorDataBody();
                    // 采集监控数据
                    List<MetricSet> harvestList = cb.harvest();
                    List<MonitorDataBody.MetricSetItem> metricSetItemList = new ArrayList<MonitorDataBody.MetricSetItem>();
                    for (MetricSet metricSet : harvestList) {
                        metricSetItemList.add(ReportDataBuilder.buildMetricSetItem(metricSet));
                    }
                    body.setCollectorName(monitorItemApp.getCollectorName());
                    body.setMonitorItemId(Integer.valueOf(monitorItemApp.getMonitorItemId().toString()));
                    body.setTimestamp(harvestTime);
                    body.setMetricSetList(metricSetItemList);
                    if (APMCollector.COLLECTOR_APM.equals(collectorName)) {
                        // javaagent自身监控数据单独发送
                        this.monitorReportService.reportInnerData(body);
                    } else {
                        // 发送数据
                        monitorReportService.offer(body);
                    }
                } else {
                    // 采集用户自定义监控数据
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "failed to harvest:", e);
            }
        }
    }

    // ~~ container methods

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public List<MonitorItem> getMonitorConfigList() {
        return monitorConfigList;
    }

    public void setMonitorConfigList(List<MonitorItem> monitorConfigList) {
        this.monitorConfigList = monitorConfigList;
    }
}
