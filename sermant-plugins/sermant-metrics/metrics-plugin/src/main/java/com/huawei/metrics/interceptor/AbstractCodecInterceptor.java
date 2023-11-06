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
import com.huawei.metrics.entity.MetricsLinkInfo;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.utils.StringUtils;

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

    private static final String ENCODE_METHOD_NAME = "encode";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!isValid(context)) {
            return context;
        }
        MetricsLinkInfo metricsLinkInfo = initLinkInfo(context);
        initIndexInfo(context);
        context.setLocalFieldValue(Constants.LINK_INFO_KEY, metricsLinkInfo);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (!isValid(context)) {
            return context;
        }
        fillMessageInfo(context);
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
     * 校验
     *
     * @param context 上下文信息
     * @return 校验结果
     */
    public abstract boolean isValid(ExecuteContext context);

    /**
     * 初始化连接信息
     *
     * @param context 上下文信息
     * @return 连接信息
     */
    public abstract MetricsLinkInfo initLinkInfo(ExecuteContext context);

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
    public MetricsLinkInfo initLinkInfo(InetSocketAddress localAddress, InetSocketAddress remoteAddress,
            String side, boolean sslEnable, String protocol) {
        String metricsKey = localAddress.getHostName() + Constants.CONNECT + remoteAddress.getHostName()
                + Constants.CONNECT + remoteAddress.getPort();
        MetricsLinkInfo metricsLinkInfo = MetricsManager.getLinkInfo(metricsKey);
        if (!StringUtils.isEmpty(metricsLinkInfo.getClientIp())) {
            return metricsLinkInfo;
        }
        metricsLinkInfo.setProtocol(protocol);
        if (isClientSide(side)) {
            initAddressAndRole(metricsLinkInfo, Constants.CLIENT_ROLE, localAddress, remoteAddress);
        } else {
            initAddressAndRole(metricsLinkInfo, Constants.SERVER_ROLE, remoteAddress, localAddress);
        }
        if (TCP_PROTOCOL.contains(metricsLinkInfo.getProtocol())) {
            metricsLinkInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + metricsLinkInfo.getL7Role());
        } else {
            metricsLinkInfo.setL4Role(Constants.UDP_PROTOCOL + Constants.CONNECT + metricsLinkInfo.getL7Role());
        }
        metricsLinkInfo.setEnableSsl(sslEnable);
        return metricsLinkInfo;
    }

    /**
     * 初始化地址和角色信息
     *
     * @param metricsLinkInfo 连接信息
     * @param role 角色信息
     * @param clientAddress 客户端地址
     * @param serverAddress 服务端地址
     */
    private void initAddressAndRole(MetricsLinkInfo metricsLinkInfo, String role, InetSocketAddress clientAddress,
            InetSocketAddress serverAddress) {
        metricsLinkInfo.setL7Role(role);
        metricsLinkInfo.setClientIp(clientAddress.getAddress().getHostAddress());
        metricsLinkInfo.setServerIp(serverAddress.getAddress().getHostAddress());
        metricsLinkInfo.setServerPort(StringUtils.getString(serverAddress.getPort()));
    }

    /**
     * 初始化渠道缓存的下标信息
     *
     * @param context 上下文信息
     */
    public abstract void initIndexInfo(ExecuteContext context);

    /**
     * 填充报文信息
     *
     * @param currentWriteIndex 当前读索引下标
     * @param currentReadIndex 当前写索引下标
     * @param context 上下文信息
     */
    protected void fillMessageInfo(int currentWriteIndex, int currentReadIndex, ExecuteContext context) {
        Object linkInfoObject = context.getLocalFieldValue(Constants.LINK_INFO_KEY);
        if (!(linkInfoObject instanceof MetricsLinkInfo)) {
            return;
        }
        MetricsLinkInfo metricsLinkInfo = (MetricsLinkInfo) linkInfoObject;
        if (StringUtils.equals(context.getMethod().getName(), ENCODE_METHOD_NAME)) {
            Object writeIndex = context.getLocalFieldValue(Constants.WRITE_INDEX_KEY);
            if (!(writeIndex instanceof Integer)) {
                return;
            }
            int addWriteIndex = currentWriteIndex - Integer.parseInt(StringUtils.getString(writeIndex));
            if (addWriteIndex > 0) {
                metricsLinkInfo.getSentBytes().addAndGet(addWriteIndex);
                metricsLinkInfo.getSentMessages().incrementAndGet();
            }
            return;
        }
        Object readIndex = context.getLocalFieldValue(Constants.READ_INDEX_KEY);
        if (!(readIndex instanceof Integer)) {
            return;
        }
        int addReadIndex = currentReadIndex - Integer.parseInt(StringUtils.getString(readIndex));
        if (addReadIndex > 0) {
            metricsLinkInfo.getReceiveBytes().addAndGet(addReadIndex);
            metricsLinkInfo.getReceiveMessages().incrementAndGet();
        }
    }

    /**
     * 填充报文信息
     *
     * @param context 上下文信息
     */
    protected abstract void fillMessageInfo(ExecuteContext context);
}
