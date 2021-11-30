package com.huawei.javamesh.core.lubanops.integration.access;

/**
 * 产生原始消息的包装器
 *
 * @author
 * @since 2020/5/5
 **/
public abstract class MessageWrapper {

    private byte[] bodyBytes = null;

    private String bodyString = null;

    public Message generatorMessage() {
        Message message = new Message();
        message.setType(getType());
        message.setMessageId(getMessageId());
        message.setHeader(getHeaderBytes());
        message.setBody(getBodyBytes());
        return message;

    }

    public byte[] getBodyBytes() {
        if (null == bodyBytes) {
            bodyBytes = getBodyString().getBytes();
        }
        return bodyBytes;
    }

    public String getBodyString() {
        if (null == bodyString || bodyString.length() == 0) {
            bodyString = generateBodyString();
        }
        return bodyString;
    }

    public abstract short getType();

    public abstract long getMessageId();

    public abstract byte[] getHeaderBytes();

    protected abstract String getHeadString();

    public abstract String generateBodyString();

}
