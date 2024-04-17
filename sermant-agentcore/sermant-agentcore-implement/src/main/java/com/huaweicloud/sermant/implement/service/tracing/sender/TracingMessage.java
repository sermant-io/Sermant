/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.tracing.sender;

import com.huaweicloud.sermant.core.service.tracing.common.SpanEvent;

/**
 * The tracing message sent to the backend node
 *
 * @author luanwenfei
 * @since 2022-03-07
 */
public class TracingMessage {
    private String messageId;

    private TracingMessageHeader header;

    private SpanEvent body;

    /**
     * Construct tracing message
     *
     * @param messageId message id
     * @param header tracing message header
     * @param body tracing message body
     */
    public TracingMessage(String messageId, TracingMessageHeader header, SpanEvent body) {
        this.messageId = messageId;
        this.header = header;
        this.body = body;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public TracingMessageHeader getHeader() {
        return header;
    }

    public void setHeader(TracingMessageHeader header) {
        this.header = header;
    }

    public SpanEvent getBody() {
        return body;
    }

    public void setBody(SpanEvent body) {
        this.body = body;
    }
}
