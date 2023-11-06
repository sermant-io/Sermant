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

package com.huawei.metrics.interceptor.alibaba;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsLinkInfo;
import com.huawei.metrics.interceptor.AbstractCodecInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;

/**
 * dubbo报文转码、解码拦截器
 *
 * @author zhp
 * @since 2023-10-17
 */
public class ExchangeCodecInterceptor extends AbstractCodecInterceptor {
    private static final int PARAM_COUNT = 2;

    @Override
    public boolean isValid(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length < PARAM_COUNT) {
            return false;
        }
        if (!(arguments[0] instanceof Channel)) {
            return false;
        }
        if (!(arguments[1] instanceof ChannelBuffer)) {
            return false;
        }
        return ((Channel) arguments[0]).getUrl() != null;
    }

    @Override
    public MetricsLinkInfo initLinkInfo(ExecuteContext context) {
        Channel channel = (Channel) context.getArguments()[0];
        URL url = channel.getUrl();
        boolean sslEnable = Boolean.parseBoolean(url.getParameter(Constants.SSL_ENABLE));
        return initLinkInfo(channel.getLocalAddress(), channel.getRemoteAddress(),
                url.getParameter(Constants.SIDE_KEY), sslEnable, url.getProtocol());
    }

    @Override
    public void initIndexInfo(ExecuteContext context) {
        ChannelBuffer buffer = (ChannelBuffer) context.getArguments()[1];
        context.setLocalFieldValue(Constants.WRITE_INDEX_KEY, buffer.writerIndex());
        context.setLocalFieldValue(Constants.READ_INDEX_KEY, buffer.readerIndex());
    }

    @Override
    protected void fillMessageInfo(ExecuteContext context) {
        ChannelBuffer buffer = (ChannelBuffer) context.getArguments()[1];
        fillMessageInfo(buffer.writerIndex(), buffer.readerIndex(), context);
    }
}
