package com.huawei.apm.core.ext.lubanops.access;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.huawei.apm.core.ext.lubanops.Constants;

/**
 * 与access通信的消息包格式，格式如下 magicNumber(2) | type(2) | messageId(8)| header
 * length(4) | body Length(4) | header | body
 * @author
 * @since 2020/5/4
 **/
public class Message {
    public static final short MAGIC_NUMBER = 9527;

    private short type;

    private long messageId;

    private byte[] header;

    private byte[] body;

    /**
     * 将二进制数组转成消息对象
     * @param bb
     * @return
     */
    public static Message parseBytes(byte[] bb) {
        if (bb.length > Constants.MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "message length exceeds max value:" + Constants.MAX_MESSAGE_LENGTH + ",actual:" + bb.length);
        }
        ByteArrayInputStream byteArrayInputStream = null;
        byteArrayInputStream = new ByteArrayInputStream(bb);
        DataInputStream dataInputStream = null;
        dataInputStream = new DataInputStream(byteArrayInputStream);
        try {
            short magic = dataInputStream.readShort();
            if (magic != MAGIC_NUMBER) {
                throw new IllegalArgumentException("magic number error");
            }
            short type = dataInputStream.readShort();
            long messageId = dataInputStream.readLong();
            int headerLength = dataInputStream.readInt();
            int bodyLength = dataInputStream.readInt();
            if (headerLength < 0) {
                throw new IllegalArgumentException("header length less than 0");
            }
            // 仍然需要再验证一下
            if (headerLength > Constants.MAX_MESSAGE_LENGTH) {
                throw new IllegalArgumentException(
                        "header length exceeds max value:" + Constants.MAX_MESSAGE_LENGTH + ",actual:" + headerLength);
            }
            byte[] header = new byte[headerLength];
            if (headerLength > 0) {
                int readLength = dataInputStream.read(header, 0, headerLength);
                if (readLength < 0) {
                    throw new IllegalArgumentException("header length less than 0");
                }
            }

            if (bodyLength < 0) {
                throw new IllegalArgumentException("body length less than 0");
            }
            // 仍然需要再验证一下
            if (bodyLength > Constants.MAX_MESSAGE_LENGTH) {
                throw new IllegalArgumentException(
                        "body length exceeds max value:" + Constants.MAX_MESSAGE_LENGTH + ",actual:" + bodyLength);
            }

            return getMessage(bodyLength, dataInputStream, type, messageId, header);

        } catch (IOException e) {
            throw new IllegalArgumentException("IOException,message length:" + bb.length);
        } finally {
            close(dataInputStream);
            close(byteArrayInputStream);
        }
    }

    private static Message getMessage(int bodyLength, DataInputStream dataInputStream, short type, long messageId,
            byte[] header) throws IOException {
        byte[] body = new byte[bodyLength];
        if (bodyLength > 0) {
            int readLength = dataInputStream.read(body, 0, bodyLength);
            if (readLength < 0) {
                throw new IllegalArgumentException("body length less than 0");
            }
        }

        Message message = new Message();
        message.setType(type);
        message.setMessageId(messageId);
        message.setBody(body);
        message.setHeader(header);
        return message;
    }

    private static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {

            }
        }
    }

    private static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {

            }
        }
    }

    /**
     * 转换成数据发送的二进制数组
     * @return
     */
    public byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            int size = 20 + header.length + body.length;
            byteArrayOutputStream = new ByteArrayOutputStream(size);
            dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeShort(MAGIC_NUMBER);
            dataOutputStream.writeShort(type);
            dataOutputStream.writeLong(messageId);
            dataOutputStream.writeInt(header.length);
            dataOutputStream.writeInt(body.length);
            if (header.length > 0) {
                dataOutputStream.write(header, 0, header.length);
            }
            if (body.length > 0) {
                dataOutputStream.write(body, 0, body.length);
            }
            dataOutputStream.flush();
            byte[] bb = byteArrayOutputStream.toByteArray();
            return bb;
        } catch (IOException e) {
            throw new RuntimeException("failed to to bytes", e);
        } finally {
            close(dataOutputStream);
            close(byteArrayOutputStream);

        }

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message)) {
            return false;
        }

        Message msg = (Message) obj;
        return this.messageId == msg.messageId && this.type == msg.type && Arrays.equals(this.header, msg.header)
                && Arrays.equals(this.body, msg.body);

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[type:" + this.type);
        stringBuilder.append("\nmessageId:" + this.messageId);
        stringBuilder.append("\nheader:" + new String(header));
        stringBuilder.append("\nbody:" + new String(body));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
