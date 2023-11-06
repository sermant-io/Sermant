/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.entity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接信息
 *
 * @author zhp
 * @since 2023-10-17
 */
public class MetricsLinkInfo extends MetricsInfo {
    /**
     * 发送的字节数
     */
    private AtomicLong sentBytes = new AtomicLong();

    /**
     * 接收的字节数
     */
    private AtomicLong receiveBytes = new AtomicLong();

    /**
     * 发送的报文数
     */
    private AtomicInteger sentMessages = new AtomicInteger();

    /**
     * 接收的报文数
     */
    private AtomicInteger receiveMessages = new AtomicInteger();

    public AtomicLong getSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(AtomicLong sentBytes) {
        this.sentBytes = sentBytes;
    }

    public AtomicLong getReceiveBytes() {
        return receiveBytes;
    }

    public void setReceiveBytes(AtomicLong receiveBytes) {
        this.receiveBytes = receiveBytes;
    }

    public AtomicInteger getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(AtomicInteger sentMessages) {
        this.sentMessages = sentMessages;
    }

    public AtomicInteger getReceiveMessages() {
        return receiveMessages;
    }

    public void setReceiveMessages(AtomicInteger receiveMessages) {
        this.receiveMessages = receiveMessages;
    }
}
