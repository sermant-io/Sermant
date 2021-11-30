package com.huawei.javamesh.core.lubanops.integration.access.outbound;

import com.huawei.javamesh.core.lubanops.integration.Constants;
import com.huawei.javamesh.core.lubanops.integration.access.Header;
import com.huawei.javamesh.core.lubanops.integration.access.MessageIdGenerator;
import com.huawei.javamesh.core.lubanops.integration.access.MessageType;
import com.huawei.javamesh.core.lubanops.integration.access.MessageWrapper;
import com.huawei.javamesh.core.lubanops.integration.utils.JSON;

/**
 * 获取agent的采集状态的请求消息
 * @author
 * @since 2020/5/7
 **/
public class CollectorStatusRequest extends MessageWrapper {
    private short type = MessageType.ACCESS_COLLECTOR_STATUS_REQUEST;

    private CollectorStatusHeader header;

    private long messageId;

    public CollectorStatusRequest(Long instanceId, String collectorName, String metricSetName) {
        CollectorStatusHeader header = new CollectorStatusHeader();
        header.setCollectorName(collectorName);
        header.setInstanceId(instanceId);
        header.setMetricSetName(metricSetName);
        header.setNeedResponse(true);
        this.header = header;
        this.messageId = MessageIdGenerator.generateMessageId();
    }

    @Override
    public short getType() {
        return MessageType.ACCESS_COLLECTOR_STATUS_REQUEST;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public String generateBodyString() {
        return "";
    }

    @Override
    public String getHeadString() {
        return JSON.toJSONString(header);
    }

    @Override
    public byte[] getHeaderBytes() {
        return header.toBytes();
    }

    @Override
    public String getBodyString() {
        throw new UnsupportedOperationException(
                CollectorStatusRequest.class.getSimpleName() + ".getBodyString() has no implementation.");
    }

    public static class CollectorStatusHeader extends Header {
        private Long instanceId;

        private String collectorName;

        private String metricSetName;

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }

        public String getCollectorName() {
            return collectorName;
        }

        public void setCollectorName(String collectorName) {
            this.collectorName = collectorName;
        }

        public String getMetricSetName() {
            return metricSetName;
        }

        public void setMetricSetName(String metricSetName) {
            this.metricSetName = metricSetName;
        }

        public byte[] toBytes() {
            return JSON.toJSONString(this).getBytes(Constants.DEFAULT_CHARSET);
        }
    }
}
