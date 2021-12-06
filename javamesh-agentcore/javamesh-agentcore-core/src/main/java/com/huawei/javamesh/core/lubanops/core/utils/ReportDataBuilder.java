/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.core.utils;

import java.util.ArrayList;
import java.util.List;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MetricSet;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.DefaultStats;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.SpanEvent;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataBody;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataHeader;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.MonitorDataBody;
import com.huawei.javamesh.core.lubanops.integration.access.inbound.MonitorDataHeader;
import com.huawei.javamesh.core.lubanops.integration.access.trace.MonitorItemRow;

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
