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

package com.huawei.jsse.interceptor.apache;

import com.huawei.jsse.common.Constants;
import com.huawei.jsse.entity.JsseLinkInfo;
import com.huawei.jsse.manager.JsseManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.buffer.ChannelBuffer;

import java.net.InetSocketAddress;

/**
 * dubbo报文加解码拦截器
 *
 * @author zhp
 * @since 2023-10-17
 */
public class CodecInterceptor implements Interceptor {
    private static final int PARAM_COUNT = 2;

    private static final String ENCODE_METHOD_NAME = "encode";

    private int writeIndex;

    private int readIndex;

    private String key;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length < PARAM_COUNT
                || !(arguments[0] instanceof Channel
                && arguments[1] instanceof ChannelBuffer)) {
            return context;
        }
        Channel channel = (Channel) arguments[0];
        ChannelBuffer buffer = (ChannelBuffer) arguments[1];
        InetSocketAddress localAddress = channel.getLocalAddress();
        InetSocketAddress remoteAddress = channel.getRemoteAddress();
        key = localAddress.getHostName() + Constants.CONNECT + remoteAddress.getAddress();
        JsseLinkInfo jsseLinkInfo = JsseManager.getJsseLink(key);
        if (StringUtils.isEmpty(jsseLinkInfo.getClientIp())) {
            initJsseLinkInfo(jsseLinkInfo, channel);
        }
        writeIndex = buffer.writerIndex();
        readIndex = buffer.readerIndex();
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length < PARAM_COUNT
                || !(arguments[0] instanceof Channel
                && arguments[1] instanceof ChannelBuffer)) {
            return context;
        }
        ChannelBuffer buffer = (ChannelBuffer) arguments[1];
        JsseLinkInfo jsseLinkInfo = JsseManager.getJsseLink(key);
        if (StringUtils.equals(context.getMethod().getName(), ENCODE_METHOD_NAME)) {
            jsseLinkInfo.getSentBytes().addAndGet(buffer.writerIndex() - writeIndex);
            jsseLinkInfo.getSentMessages().incrementAndGet();
            return context;
        }
        if (buffer.readerIndex() - readIndex > 0) {
            jsseLinkInfo.getReceiveBytes().addAndGet(buffer.readerIndex() - readIndex);
            jsseLinkInfo.getReceiveMessages().incrementAndGet();
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    /**
     * 初始化jsse连接信息
     *
     * @param jsseLinkInfo 连接信息
     * @param channel 渠道信息
     */
    private void initJsseLinkInfo(JsseLinkInfo jsseLinkInfo, Channel channel) {
        jsseLinkInfo.setClientIp(channel.getLocalAddress().getHostName());
        jsseLinkInfo.setServerIp(channel.getRemoteAddress().getHostName());
        jsseLinkInfo.setServerPort(StringUtils.getString(channel.getRemoteAddress().getPort()));
        jsseLinkInfo.setProtocol(channel.getUrl().getProtocol());
        if (isClientSide(channel)) {
            jsseLinkInfo.setL7Role(Constants.CLIENT_ROLE);
        } else {
            jsseLinkInfo.setL7Role(Constants.SERVER_ROLE);
        }
        if (Constants.TCP_PROTOCOLS.contains(jsseLinkInfo.getProtocol())) {
            jsseLinkInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + jsseLinkInfo.getL7Role());
        } else {
            jsseLinkInfo.setL4Role(Constants.UDP_PROTOCOL + Constants.CONNECT + jsseLinkInfo.getL7Role());
        }
        jsseLinkInfo.setEnableSsl(Boolean.parseBoolean(channel.getUrl().getParameter(Constants.SSL_ENABLE)));
    }

    /**
     * 是否为客户端
     *
     * @param channel 渠道信息
     * @return 是否为客户端的结果
     */
    private boolean isClientSide(Channel channel) {
        String side = (String) channel.getAttribute(CommonConstants.SIDE_KEY);
        if (Constants.CLIENT_ROLE.equals(side)) {
            return true;
        }
        if (Constants.SERVER_ROLE.equals(side)) {
            return false;
        }
        InetSocketAddress address = channel.getRemoteAddress();
        URL url = channel.getUrl();
        return url.getPort() == address.getPort() && NetUtils.filterLocalHost(url.getIp())
                .equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
    }
}
