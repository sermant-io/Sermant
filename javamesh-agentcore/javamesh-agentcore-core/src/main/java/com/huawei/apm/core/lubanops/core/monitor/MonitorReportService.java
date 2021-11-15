package com.huawei.apm.core.lubanops.core.monitor;

import com.huawei.apm.core.lubanops.integration.access.inbound.MonitorDataBody;

/**
 * @author
 * @date 2020/10/29 20:00
 */
public interface MonitorReportService {

    boolean offer(MonitorDataBody body);

    void reportInnerData(MonitorDataBody body);
}
