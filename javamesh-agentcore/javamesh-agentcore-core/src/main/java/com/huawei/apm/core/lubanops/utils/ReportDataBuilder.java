package com.huawei.apm.core.lubanops.utils;

import java.util.ArrayList;
import java.util.List;

import com.huawei.apm.bootstrap.lubanops.collector.api.MetricSet;
import com.huawei.apm.bootstrap.lubanops.collector.api.MonitorDataRow;
import com.huawei.apm.bootstrap.lubanops.config.IdentityConfigManager;
import com.huawei.apm.bootstrap.lubanops.plugin.common.DefaultStats;
import com.huawei.apm.bootstrap.lubanops.trace.SpanEvent;
import com.huawei.apm.core.ext.lubanops.access.inbound.EventDataBody;
import com.huawei.apm.core.ext.lubanops.access.inbound.EventDataHeader;
import com.huawei.apm.core.ext.lubanops.access.inbound.MonitorDataBody;
import com.huawei.apm.core.ext.lubanops.access.inbound.MonitorDataHeader;
import com.huawei.apm.core.ext.lubanops.access.trace.MonitorItemRow;

/**
 * @author
 * @date 2020/9/24 14:56
 */
public class ReportDataBuilder {

    public static EventDataHeader buildEventDataHeader() {
        EventDataHeader header = new EventDataHeader();
        header.setEnvId(IdentityConfigManager.getEnvId());
        header.setInstanceId(IdentityConfigManager.getInstanceId());
        header.setAppId(IdentityConfigManager.getAppId());
        header.setBizId(IdentityConfigManager.getBizId());
        header.setDomainId(IdentityConfigManager.getDomainId());
        header.setAttachment(IdentityConfigManager.getAttachment());
        return header;
    }

    public static MonitorDataHeader buildMonitorDataHeader() {
        MonitorDataHeader header = new MonitorDataHeader();
        header.setEnvId(IdentityConfigManager.getEnvId());
        header.setInstanceId(IdentityConfigManager.getInstanceId());
        header.setAppId(IdentityConfigManager.getAppId());
        header.setBizId(IdentityConfigManager.getBizId());
        header.setDomainId(IdentityConfigManager.getDomainId());
        header.setAttachment(IdentityConfigManager.getAttachment());
        return header;
    }

    public static EventDataBody buildEventDataBody(SpanEvent spanEvent) {
        EventDataBody body = new EventDataBody();
        body.setGlobalTraceId(spanEvent.getGlobalTraceId());
        body.setGlobalPath(spanEvent.getGlobalPath());
        body.setTraceId(spanEvent.getTraceId());
        body.setSpanId(spanEvent.getSpanId());
        body.setEventId(spanEvent.getEventId());
        body.setNextSpanId(spanEvent.getNextSpanId());
        body.setClassName(spanEvent.getClassName());
        body.setMethod(spanEvent.getMethod());
        body.setType(spanEvent.getType());
        body.setStartTime(spanEvent.getStartTime());
        body.setTimeUsed(spanEvent.getTimeUsed());
        body.setChildrenEventCount(spanEvent.getChildrenEventCount());
        body.setHasError(spanEvent.getHasError());
        body.setErrorReasons(spanEvent.getErrorReasons());
        body.setSource(spanEvent.getSource());
        body.setRealSource(spanEvent.getRealSource());
        body.setAsync(spanEvent.isAsync());
        body.setTags(spanEvent.getTags());
        body.setCode(spanEvent.getCode());
        body.setArgument(spanEvent.getArgument());
        body.setSourceEventId(spanEvent.getSourceEventId());
        List<SpanEvent.DiscardInfo> discardInfos = spanEvent.getDiscard();
        List<EventDataBody.DiscardInfo> eventDataDiscardInfos = new ArrayList<EventDataBody.DiscardInfo>();
        for (SpanEvent.DiscardInfo discardInfo : discardInfos) {
            EventDataBody.DiscardInfo eventDataDiscardInfo = new EventDataBody.DiscardInfo();
            eventDataDiscardInfo.setCount(discardInfo.getCount());
            eventDataDiscardInfo.setTotalTime(discardInfo.getTotalTime() / DefaultStats.NANO_TO_MILLI);
            eventDataDiscardInfo.setType(discardInfo.getType());
            eventDataDiscardInfos.add(eventDataDiscardInfo);
        }
        body.setDiscard(eventDataDiscardInfos);
        return body;
    }

    public static MonitorDataBody.MetricSetItem buildMetricSetItem(MetricSet metricSet) {
        MonitorDataBody.MetricSetItem monitorItem = new MonitorDataBody.MetricSetItem();
        monitorItem.setCode(metricSet.getCode());
        monitorItem.setName(metricSet.getName());
        monitorItem.setAttachment(metricSet.getAttachment());
        monitorItem.setMsg(metricSet.getMsg());
        List<MonitorItemRow> itemRows = new ArrayList<MonitorItemRow>();
        List<MonitorDataRow> rows = metricSet.getDataRows();
        if (rows != null) {
            for (MonitorDataRow dataRow : metricSet.getDataRows()) {
                itemRows.add(transferToItemRow(dataRow));
            }
            monitorItem.setDataRows(itemRows);
        }
        return monitorItem;
    }

    private static MonitorItemRow transferToItemRow(MonitorDataRow dataRow) {
        MonitorItemRow itemRow = new MonitorItemRow();
        itemRow.setEntries(dataRow);
        itemRow.setTime(System.currentTimeMillis());
        return itemRow;
    }
}
