package com.huawei.javamesh.core.lubanops.integration.access.inbound;

import com.huawei.javamesh.core.lubanops.integration.access.Message;
import com.huawei.javamesh.core.lubanops.integration.access.MessageType;
import com.huawei.javamesh.core.lubanops.integration.access.MessageWrapper;
import com.huawei.javamesh.core.lubanops.integration.utils.JSON;

/**
 * @author
 * @since 2020/5/7
 **/
public class EventDataRequest extends MessageWrapper {
    private EventDataHeader header;

    private EventDataBody body;

    private long messageId;

    /**
     * 解析成对象
     * @param message
     * @return
     */
    public static EventDataRequest parse(Message message) {
        if (message.getType() != MessageType.TRACE_EVENT_REQUEST) {
            throw new IllegalArgumentException("type not match");
        }
        try {
            EventDataRequest request = new EventDataRequest();

            EventDataHeader head = JSON.parseObject(message.getHeader(), EventDataHeader.class);
            EventDataBody body = JSON.parseObject(message.getBody(), EventDataBody.class);
            request.setMessageId(message.getMessageId());
            request.setHeader(head);
            request.setBody(body);
            return request;
        } catch (Exception e) {
            throw new RuntimeException("failed to parse msg,msg type:" + message.getType(), e);
        }
    }

    public static EventDataHeader parseHeader(Message message) {
        return JSON.parseObject(message.getHeader(), EventDataHeader.class);
    }

    public EventDataHeader getHeader() {
        return header;
    }

    public void setHeader(EventDataHeader header) {
        this.header = header;
    }

    public EventDataBody getBody() {
        return body;
    }

    public void setBody(EventDataBody body) {
        this.body = body;
    }

    @Override
    public short getType() {
        return MessageType.TRACE_EVENT_REQUEST;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public byte[] getHeaderBytes() {
        return header.toBytes();
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public String getHeadString() {
        return JSON.toJSONString(header);
    }

    @Override
    public String generateBodyString() {
        return JSON.toJSONString(body);
    }
}
