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
