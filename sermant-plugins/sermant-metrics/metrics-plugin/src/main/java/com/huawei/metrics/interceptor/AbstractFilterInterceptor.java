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

package com.huawei.metrics.interceptor;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.net.InetSocketAddress;

/**
 * dubbo服务过滤器拦截器
 *
 * @author zhp
 * @since 2023-10-17
 */
public abstract class AbstractFilterInterceptor implements Interceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!isValid(context)) {
            return context;
        }
        long startTime = System.nanoTime();
        MetricsRpcInfo metricsRpcInfo = initRpcInfo(context);
        metricsRpcInfo.getReqCount().incrementAndGet();
        context.setLocalFieldValue(Constants.START_TIME_KEY, startTime);
        context.setLocalFieldValue(Constants.RPC_INFO_KEY, metricsRpcInfo);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (!isValid(context)) {
            return context;
        }
        Object startTimeObject = context.getLocalFieldValue(Constants.START_TIME_KEY);
        if (!(startTimeObject instanceof Long)) {
            return context;
        }
        Object metrcisObject = context.getLocalFieldValue(Constants.RPC_INFO_KEY);
        if (!(metrcisObject instanceof MetricsRpcInfo)) {
            return context;
        }
        long startTime = Long.parseLong(StringUtils.getString(startTimeObject));
        MetricsRpcInfo metricsRpcInfo = (MetricsRpcInfo) metrcisObject;
        metricsRpcInfo.getResponseCount().incrementAndGet();
        long latency = System.nanoTime() - startTime;
        metricsRpcInfo.getSumLatency().addAndGet(latency);
        metricsRpcInfo.getLatencyList().add(latency);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (!isValid(context)) {
            return context;
        }
        Object metrcisObject = context.getLocalFieldValue(Constants.RPC_INFO_KEY);
        if (!(metrcisObject instanceof MetricsRpcInfo)) {
            return context;
        }
        MetricsRpcInfo metricsRpcInfo = (MetricsRpcInfo) metrcisObject;
        metricsRpcInfo.getReqErrorCount().incrementAndGet();
        return context;
    }

    /**
     * 是否为客户端
     *
     * @param side 客户端服务端标识
     * @return 是否为客户端的结果
     */
    private boolean isClientSide(String side) {
        return Constants.CONSUMER_SIDE.equals(side);
    }

    /**
     * 校验
     *
     * @param context 上下文信息
     * @return 校验结果
     */
    public abstract boolean isValid(ExecuteContext context);

    /**
     * 初始化调用信息
     *
     * @param context 上下文信息
     * @return 调用信息
     */
    public abstract MetricsRpcInfo initRpcInfo(ExecuteContext context);

    /**
     * 初始化PRC信息
     *
     * @param localAddress 本地地址
     * @param remoteAddress 远程地址
     * @param side 消费端或者服务端标识
     * @param sslEnable 是否开启SSL
     * @param protocol 协议信息
     * @return 填充的RPC信息
     */
    public MetricsRpcInfo initRpcInfo(InetSocketAddress localAddress, InetSocketAddress remoteAddress,
            String side, boolean sslEnable, String protocol) {
        String metricsKey = localAddress.getHostName() + Constants.CONNECT + remoteAddress.getHostName()
                + Constants.CONNECT + remoteAddress.getPort();
        MetricsRpcInfo metricsRpcInfo = MetricsManager.getRpcInfo(metricsKey);
        if (!StringUtils.isEmpty(metricsRpcInfo.getClientIp())) {
            return metricsRpcInfo;
        }
        metricsRpcInfo.setProtocol(protocol);
        if (isClientSide(side)) {
            initAddressAndRole(metricsRpcInfo, localAddress, remoteAddress, Constants.CLIENT_ROLE);
        } else {
            initAddressAndRole(metricsRpcInfo, remoteAddress, localAddress, Constants.SERVER_ROLE);
        }
        if (Constants.TCP_PROTOCOLS.contains(metricsRpcInfo.getProtocol())) {
            metricsRpcInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        } else {
            metricsRpcInfo.setL4Role(Constants.UDP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        }
        metricsRpcInfo.setEnableSsl(sslEnable);
        return metricsRpcInfo;
    }

    /**
     * 初始化地址信息和角色
     *
     * @param metricsRpcInfo RPC信息
     * @param clientAddress 客户端地址信息
     * @param serverAddress 服务端地址信息
     * @param role 角色信息
     */
    private void initAddressAndRole(MetricsRpcInfo metricsRpcInfo, InetSocketAddress clientAddress,
            InetSocketAddress serverAddress, String role) {
        metricsRpcInfo.setClientIp(clientAddress.getHostName());
        metricsRpcInfo.setServerIp(serverAddress.getHostName());
        metricsRpcInfo.setServerPort(StringUtils.getString(serverAddress.getPort()));
        metricsRpcInfo.setL7Role(role);
    }
}
