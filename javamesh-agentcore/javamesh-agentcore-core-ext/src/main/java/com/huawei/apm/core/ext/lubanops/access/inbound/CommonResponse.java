package com.huawei.apm.core.ext.lubanops.access.inbound;

import com.huawei.apm.core.ext.lubanops.access.MessageWrapper;

/**
 * 通用的response信息，header和body都为空，主要用于应答
 *
 * @author
 * @since 2020/5/7
 **/
public class CommonResponse extends MessageWrapper {

    private short messageType;

    private long msgId;

    public CommonResponse(short messageType, long msgId) {
        this.messageType = messageType;
        this.msgId = msgId;
    }

    @Override
    public short getType() {
        return messageType;
    }

    @Override
    public long getMessageId() {
        return msgId;
    }

    @Override
    public byte[] getHeaderBytes() {
        return new byte[0];
    }

    @Override
    public String generateBodyString() {
        return "";
    }

    @Override
    public String getHeadString() {
        throw new UnsupportedOperationException(
            CommonResponse.class.getSimpleName() + ".getBodyString() has no implementation.");
    }

    @Override
    public String getBodyString() {
        throw new UnsupportedOperationException(
            CommonResponse.class.getSimpleName() + ".getBodyString() has no implementation.");
    }

}
