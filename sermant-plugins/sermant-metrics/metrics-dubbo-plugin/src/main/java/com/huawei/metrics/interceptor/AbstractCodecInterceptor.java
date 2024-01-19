/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.interceptor;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.common.ResultType;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.util.ResultJudgmentUtil;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * dubbo报文加解码拦截器
 *
 * @author zhp
 * @since 2023-10-17
 */
public abstract class AbstractCodecInterceptor implements Interceptor {
    private static final Set<String> TCP_PROTOCOL = new HashSet<String>() {
        {
            add("dubbo");
            add("rmi");
            add("http");
            add("https");
        }
    };

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    /**
     * 是否为客户端
     *
     * @param side 客户端、服务端标识
     * @return 是否为客户端的结果
     */
    private boolean isClientSide(String side) {
        return Constants.CONSUMER_SIDE.equals(side);
    }

    /**
     * 初始化连接信息
     *
     * @param localAddress 本地地址
     * @param remoteAddress 远程地址
     * @param side 消费端或者服务端标识
     * @param sslEnable 是否开启SSL
     * @param protocol 协议信息
     * @return 连接信息
     */
    public MetricsRpcInfo initRpcInfo(InetSocketAddress localAddress, InetSocketAddress remoteAddress,
            String side, boolean sslEnable, String protocol) {
        MetricsRpcInfo metricsRpcInfo = new MetricsRpcInfo();
        metricsRpcInfo.setProtocol(protocol);
        if (isClientSide(side)) {
            initAddressAndRole(metricsRpcInfo, Constants.CLIENT_ROLE, localAddress, remoteAddress);
        } else {
            initAddressAndRole(metricsRpcInfo, Constants.SERVER_ROLE, remoteAddress, localAddress);
        }
        if (TCP_PROTOCOL.contains(metricsRpcInfo.getProtocol())) {
            metricsRpcInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        } else {
            metricsRpcInfo.setL4Role(Constants.UDP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        }
        metricsRpcInfo.setEnableSsl(sslEnable);
        return metricsRpcInfo;
    }

    /**
     * 初始化地址和角色信息
     *
     * @param metricsRpcInfo RPC指标信息
     * @param role 角色信息
     * @param clientAddress 客户端地址
     * @param serverAddress 服务端地址
     */
    private void initAddressAndRole(MetricsRpcInfo metricsRpcInfo, String role, InetSocketAddress clientAddress,
            InetSocketAddress serverAddress) {
        metricsRpcInfo.setL7Role(role);
        metricsRpcInfo.setClientIp(clientAddress.getAddress().getHostAddress());
        metricsRpcInfo.setServerIp(serverAddress.getAddress().getHostAddress());
        metricsRpcInfo.setServerPort(serverAddress.getPort());
    }

    /**
     * 填充错误统计信息
     *
     * @param status 响应编码
     * @param metricsRpcInfo 指标信息
     */
    public void fillErrorCountInfo(byte status, MetricsRpcInfo metricsRpcInfo) {
        int value = ResultJudgmentUtil.judgeDubboResult(status);
        if (value == ResultType.SUCCESS.getValue()) {
            return;
        }
        if (value == ResultType.CLIENT_ERROR.getValue()) {
            metricsRpcInfo.getClientErrorCount().getAndIncrement();
            metricsRpcInfo.getReqErrorCount().getAndIncrement();
            return;
        }
        if (value == ResultType.SERVER_ERROR.getValue()) {
            metricsRpcInfo.getServerErrorCount().getAndIncrement();
            metricsRpcInfo.getReqErrorCount().getAndIncrement();
        }
    }
}
