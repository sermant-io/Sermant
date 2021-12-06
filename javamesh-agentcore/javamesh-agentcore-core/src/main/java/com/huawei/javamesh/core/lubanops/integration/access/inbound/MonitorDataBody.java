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

package com.huawei.javamesh.core.lubanops.integration.access.inbound;

import java.util.List;

import com.huawei.javamesh.core.lubanops.integration.access.Body;
import com.huawei.javamesh.core.lubanops.integration.access.trace.MonitorItemRow;

/**
 * 监控数据的body，每一个body采集一个监控项数据
 *
 * @author
 * @since 2020/4/30
 **/
public class MonitorDataBody extends Body {
    /**
     * 这个是javaagent的客户端的时间
     */
    private long timestamp;

    private int monitorItemId;

    private String collectorName;

    private List<MetricSetItem> metricSetList;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public List<MetricSetItem> getMetricSetList() {
        return metricSetList;
    }

    public void setMetricSetList(List<MetricSetItem> metricSetList) {
        this.metricSetList = metricSetList;
    }

    public int getMonitorItemId() {
        return monitorItemId;
    }

    public void setMonitorItemId(int monitorItemId) {
        this.monitorItemId = monitorItemId;
    }

    /**
     * 指标集的数据列表
     *
     * @author yefeng
     */
    public static class MetricSetItem {
        private String name;

        /**
         * 错误码，正确的时候为0，错误的非0值
         */
        private int code;

        /**
         * 错误的时候的错误消息
         */
        private String msg;

        /**
         * 附属信息，如果主键满了的情况，可以用这个接口上报信息
         */
        private String attachment;

        private List<MonitorItemRow> dataRows;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public List<MonitorItemRow> getDataRows() {
            return dataRows;
        }

        public void setDataRows(List<MonitorItemRow> dataRows) {
            this.dataRows = dataRows;
        }

        public String getAttachment() {
            return attachment;
        }

        public void setAttachment(String attachment) {
            this.attachment = attachment;
        }

    }

}
