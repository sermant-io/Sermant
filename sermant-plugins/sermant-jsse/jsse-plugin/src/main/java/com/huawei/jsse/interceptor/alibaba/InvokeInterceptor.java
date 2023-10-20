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

package com.huawei.jsse.interceptor.alibaba;

import com.huawei.jsse.common.Constants;
import com.huawei.jsse.entity.JsseRpcInfo;
import com.huawei.jsse.manager.JsseManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.AtomicPositiveInteger;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * dubbo服务调用拦截器
 *
 * @author zhp
 * @since 2023-10-17
 */
public class InvokeInterceptor implements Interceptor {
    private long startTime;

    private String key;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Optional<Object> indexOptional = ReflectUtils.getFieldValue(context.getObject(), Constants.CLIENT_INDEX);
        Optional<Object> clientOptional = ReflectUtils.getFieldValue(context.getObject(), Constants.CLIENTS_NAME);
        if (!indexOptional.isPresent() || !clientOptional.isPresent()
                || !(indexOptional.get() instanceof AtomicPositiveInteger)
                || !(clientOptional.get() instanceof ExchangeClient[])) {
            return context;
        }
        AtomicPositiveInteger index = (AtomicPositiveInteger) indexOptional.get();
        ExchangeClient[] clients = (ExchangeClient[]) clientOptional.get();
        ExchangeClient client = clients[index.get() % clients.length];
        startTime = System.currentTimeMillis();
        JsseRpcInfo jsseRpcInfo = initJsseRpcInfo(client);
        jsseRpcInfo.getReqCount().incrementAndGet();
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        JsseRpcInfo jsseRpcInfo = JsseManager.getJsseRpc(key);
        jsseRpcInfo.getResponseCount().incrementAndGet();
        long latency = System.currentTimeMillis() - startTime;
        jsseRpcInfo.getSumLatency().addAndGet(latency);
        jsseRpcInfo.getLatencyList().add(latency);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        JsseRpcInfo jsseRpcInfo = JsseManager.getJsseRpc(key);
        jsseRpcInfo.getReqErrorCount().incrementAndGet();
        return context;
    }

    /**
     * 初始化jsse调用信息
     *
     * @param channel 渠道信息
     * @return 调用信息
     */
    private JsseRpcInfo initJsseRpcInfo(Channel channel) {
        InetSocketAddress localAddress = channel.getLocalAddress();
        InetSocketAddress remoteAddress = channel.getRemoteAddress();
        key = localAddress.getHostName() + Constants.CONNECT + remoteAddress.getAddress();
        JsseRpcInfo jsseRpcInfo = JsseManager.getJsseRpc(key);
        if (!StringUtils.isEmpty(jsseRpcInfo.getClientIp())) {
            return jsseRpcInfo;
        }
        jsseRpcInfo.setClientIp(channel.getLocalAddress().getHostName());
        jsseRpcInfo.setServerIp(channel.getRemoteAddress().getHostName());
        jsseRpcInfo.setServerPort(StringUtils.getString(channel.getRemoteAddress().getPort()));
        jsseRpcInfo.setProtocol(channel.getUrl().getProtocol());
        if (isClientSide(channel)) {
            jsseRpcInfo.setL7Role(Constants.CLIENT_ROLE);
        } else {
            jsseRpcInfo.setL7Role(Constants.SERVER_ROLE);
        }
        if (Constants.TCP_PROTOCOLS.contains(jsseRpcInfo.getProtocol())) {
            jsseRpcInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + jsseRpcInfo.getL7Role());
        } else {
            jsseRpcInfo.setL4Role(Constants.UDP_PROTOCOL + Constants.CONNECT + jsseRpcInfo.getL7Role());
        }
        jsseRpcInfo.setEnableSsl(Boolean.parseBoolean(channel.getUrl().getParameter(Constants.SSL_ENABLE)));
        return jsseRpcInfo;
    }

    /**
     * 是否为客户端
     *
     * @param channel 渠道信息
     * @return 是否为客户端的结果
     */
    private boolean isClientSide(Channel channel) {
        String side = (String) channel.getAttribute(com.alibaba.dubbo.common.Constants.SIDE_KEY);
        if (Constants.CLIENT_ROLE.equals(side)) {
            return true;
        } else if (Constants.SERVER_ROLE.equals(side)) {
            return false;
        } else {
            InetSocketAddress address = channel.getRemoteAddress();
            URL url = channel.getUrl();
            return url.getPort() == address.getPort() && NetUtils.filterLocalHost(url.getIp())
                    .equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
        }
    }
}
