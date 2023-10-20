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

package com.huawei.jsse.entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Jsse连接信息
 *
 * @author zhp
 * @since 2023-10-17
 */
public class JsseLinkInfo extends JsseInfo {
    /**
     * 发送的字节数
     */
    private AtomicInteger sentBytes = new AtomicInteger();

    /**
     * 接收的字节数
     */
    private AtomicInteger receiveBytes = new AtomicInteger();

    /**
     * 发送的报文数
     */
    private AtomicInteger sentMessages = new AtomicInteger();

    /**
     * 接收的报文数
     */
    private AtomicInteger receiveMessages = new AtomicInteger();

    public AtomicInteger getSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(AtomicInteger sentBytes) {
        this.sentBytes = sentBytes;
    }

    public AtomicInteger getReceiveBytes() {
        return receiveBytes;
    }

    public void setReceiveBytes(AtomicInteger receiveBytes) {
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
