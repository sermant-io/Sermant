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

import com.huawei.sermant.core.lubanops.integration.access.MessageWrapper;

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
