package com.huawei.apm.core.lubanops.integration.access.inbound;

import com.huawei.apm.core.lubanops.integration.access.Message;
import com.huawei.apm.core.lubanops.integration.access.MessageType;
import com.huawei.apm.core.lubanops.integration.access.MessageWrapper;
import com.huawei.apm.core.lubanops.integration.utils.JSON;

/**
 * 监控数据上报的消息 javaagent上报的数据格式
 * @author yefeng
 */
public class MonitorDataRequest extends MessageWrapper {

    private MonitorDataHeader header;

    private MonitorDataBody body;

    private long messageId;

    public MonitorDataRequest() {

    }

    /**
     * 将二进制原始数据反序列化
     * @param msg
     * @return
     */
    public static MonitorDataRequest parseMessage(Message msg) {

        if (msg.getType() != MessageType.MONITOR_DATA_REQUEST) {
            throw new IllegalArgumentException("type not match");
        }
        try {
            MonitorDataRequest report = new MonitorDataRequest();
            MonitorDataHeader header = JSON.parseObject(msg.getHeader(), MonitorDataHeader.class);
            MonitorDataBody body = JSON.parseObject(msg.getBody(), MonitorDataBody.class);
            report.setMessageId(msg.getMessageId());
            report.setHeader(header);
            report.setBody(body);
            return report;
        } catch (Exception e) {
            throw new RuntimeException("failed to parse msg,msg type:" + msg.getType(), e);
        }

    }

    /**
     * 只解析头部信息，主要用于access的计算
     * @param msg
     * @return
     */
    public static MonitorDataHeader parseHeader(Message msg) {
        MonitorDataHeader header = JSON.parseObject(msg.getHeader(), MonitorDataHeader.class);
        return header;
    }

    @Override
    public short getType() {
        return MessageType.MONITOR_DATA_REQUEST;
    }

    @Override
    public byte[] getHeaderBytes() {
        return header.toBytes();
    }

    @Override
    public String generateBodyString() {
        return JSON.toJSONString(body);
    }

    public MonitorDataHeader getHeader() {
        return header;
    }

    public void setHeader(MonitorDataHeader header) {
        this.header = header;
    }

    public MonitorDataBody getBody() {
        return body;
    }

    public void setBody(MonitorDataBody body) {
        this.body = body;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String getHeadString() {
        return JSON.toJSONString(header);
    }

    @Override
    public String getBodyString() {
        return JSON.toJSONString(body);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitorDataRequest{");
        sb.append("header=").append(header);
        sb.append(", body=").append(body);
        sb.append(", messageId=").append(messageId);
        sb.append('}');
        return sb.toString();
    }
}
