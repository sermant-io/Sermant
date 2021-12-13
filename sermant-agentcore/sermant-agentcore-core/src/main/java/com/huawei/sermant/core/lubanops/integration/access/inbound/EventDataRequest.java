/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.integration.access.inbound;

import com.huawei.sermant.core.lubanops.integration.access.Message;
import com.huawei.sermant.core.lubanops.integration.access.MessageType;
import com.huawei.sermant.core.lubanops.integration.access.MessageWrapper;
import com.huawei.sermant.core.lubanops.integration.utils.JSON;

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
