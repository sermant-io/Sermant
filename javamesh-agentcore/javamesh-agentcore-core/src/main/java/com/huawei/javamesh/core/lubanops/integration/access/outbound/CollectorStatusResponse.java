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

package com.huawei.javamesh.core.lubanops.integration.access.outbound;

import com.huawei.javamesh.core.lubanops.integration.access.Body;
import com.huawei.javamesh.core.lubanops.integration.access.Header;
import com.huawei.javamesh.core.lubanops.integration.access.Message;
import com.huawei.javamesh.core.lubanops.integration.access.MessageType;
import com.huawei.javamesh.core.lubanops.integration.access.MessageWrapper;
import com.huawei.javamesh.core.lubanops.integration.utils.JSON;

/**
 * @author
 * @since 2020/5/7
 **/
public class CollectorStatusResponse extends MessageWrapper {
    private long messageId;

    private CollectorStatusResponseHeader header;

    private CollectorStatusResponseBody body;

    private CollectorStatusResponse() {

    }

    /**
     * 解析成对象
     * @param message
     * @return
     */
    public static CollectorStatusResponse parse(Message message) {
        if (message.getType() != MessageType.ACCESS_COLLECTOR_STATUS_RESPONSE) {
            throw new IllegalArgumentException("type not match");
        }
        try {
            CollectorStatusResponse response = new CollectorStatusResponse();

            CollectorStatusResponseHeader header = JSON.parseObject(message.getHeader(),
                    CollectorStatusResponseHeader.class);
            CollectorStatusResponseBody body = JSON.parseObject(message.getBody(),
                    CollectorStatusResponseBody.class);
            response.setMessageId(message.getMessageId());
            response.setHeader(header);
            response.setBody(body);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("failed to parse msg,msg type:" + message.getType(), e);
        }
    }

    public CollectorStatusResponseHeader getHeader() {
        return header;
    }

    public void setHeader(CollectorStatusResponseHeader header) {
        this.header = header;
    }

    public CollectorStatusResponseBody getBody() {
        return body;
    }

    public void setBody(CollectorStatusResponseBody body) {
        this.body = body;
    }

    @Override
    public short getType() {
        return MessageType.ACCESS_COLLECTOR_STATUS_RESPONSE;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public byte[] getHeaderBytes() {
        return header.toBytes();
    }

    @Override
    public String generateBodyString() {
        return JSON.toJSONString(body);
    }

    @Override
    public String getHeadString() {
        return JSON.toJSONString(header);
    }

    @Override
    public String getBodyString() {
        throw new UnsupportedOperationException(
                CollectorStatusResponse.class.getSimpleName() + ".getBodyString() has no implementation.");
    }

    /**
     * 返回的头部信息
     */
    public static class CollectorStatusResponseHeader extends Header {

    }

    /**
     * 返回的body信息
     */
    public static class CollectorStatusResponseBody extends Body {

    }
}
