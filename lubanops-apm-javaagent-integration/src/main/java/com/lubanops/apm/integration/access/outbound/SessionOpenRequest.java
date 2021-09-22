package com.lubanops.apm.integration.access.outbound;

import com.lubanops.apm.integration.access.Header;
import com.lubanops.apm.integration.access.MessageIdGenerator;
import com.lubanops.apm.integration.access.MessageType;
import com.lubanops.apm.integration.access.MessageWrapper;
import com.lubanops.apm.integration.utils.JSON;

/**
 * @author
 * @since 2020/5/5
 **/
public class SessionOpenRequest extends MessageWrapper {

    private long messageId = MessageIdGenerator.generateMessageId();

    private SessionOpenHeader header;

    public SessionOpenRequest() {
    }

    public SessionOpenRequest(String code, String msg, Long instanceId) {
        SessionOpenHeader header = new SessionOpenHeader(code, msg, instanceId);
        this.header = header;

    }

    public SessionOpenHeader getHeader() {
        return header;
    }

    public void setHeader(SessionOpenHeader header) {
        this.header = header;
    }

    @Override
    public short getType() {
        return MessageType.ACCESS_SESSION_OPEN_RESPONSE;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public byte[] getHeaderBytes() {
        return header.toBytes();
    }

    @Override
    public String getHeadString() {
        return JSON.toJSONString(header);
    }

    @Override
    public String generateBodyString() {
        return "";
    }

    @Override
    public String getBodyString() {
        throw new UnsupportedOperationException(
                SessionOpenRequest.class.getSimpleName() + ".getBodyString() has no implementation.");
    }

    /**
     * 头部信息
     */
    public static class SessionOpenHeader extends Header {
        public static String ok = "0";

        public static String error = "1";

        // 0 代表成功，1 代表失败
        private String code = "0";

        private String msg;

        private String instanceId;

        public SessionOpenHeader() {
        }

        public SessionOpenHeader(String code, String msg, Long instanceId) {
            this.code = code;
            this.msg = msg;
            this.instanceId = instanceId.toString();
        }

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }
}
