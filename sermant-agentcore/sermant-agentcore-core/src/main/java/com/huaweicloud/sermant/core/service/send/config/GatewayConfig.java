/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.service.send.config;

import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;

/**
 * gateway配置
 *
 * @author luanwenfei
 * @since 2022-03-24
 */
@ConfigTypeKey("gateway")
public class GatewayConfig implements BaseConfig {
    private static final int NETTY_DEFAULT_CONNECT_TIMEOUT = 5000;

    private static final long NETTY_DEFAULT_WRITE_READ_WAIT_TIME = 60000L;

    /**
     * netty服务端的地址
     */
    private String nettyIp;

    /**
     * netty服务端的端口
     */
    private int nettyPort;

    /**
     * netty发送消息的间隔，单位：秒
     */
    private int sendInternalTime;

    /**
     * netty连接后断开的初始重连时间，单位：秒
     */
    private int initReconnectInternalTime;

    /**
     * netty连接后断开的最大重连时间，单位：秒
     */
    private int maxReconnectInternalTime;

    /**
     * Netty 需要设置Integer型超时事件，故此处为int非long
     */
    private int nettyConnectTimeout = NETTY_DEFAULT_CONNECT_TIMEOUT;

    private long nettyWriteAndReadWaitTime = NETTY_DEFAULT_WRITE_READ_WAIT_TIME;

    public String getNettyIp() {
        return nettyIp;
    }

    public void setNettyIp(String nettyIp) {
        this.nettyIp = nettyIp;
    }

    public int getNettyPort() {
        return nettyPort;
    }

    public void setNettyPort(int nettyPort) {
        this.nettyPort = nettyPort;
    }

    public int getNettyConnectTimeout() {
        return nettyConnectTimeout;
    }

    public void setNettyConnectTimeout(int nettyConnectTimeout) {
        this.nettyConnectTimeout = nettyConnectTimeout;
    }

    public long getNettyWriteAndReadWaitTime() {
        return nettyWriteAndReadWaitTime;
    }

    public void setNettyWriteAndReadWaitTime(long nettyWriteAndReadWaitTime) {
        this.nettyWriteAndReadWaitTime = nettyWriteAndReadWaitTime;
    }

    public int getSendInternalTime() {
        return sendInternalTime;
    }

    public void setSendInternalTime(int sendInternalTime) {
        this.sendInternalTime = sendInternalTime;
    }

    public int getInitReconnectInternalTime() {
        return initReconnectInternalTime;
    }

    public void setInitReconnectInternalTime(int initReconnectInternalTime) {
        this.initReconnectInternalTime = initReconnectInternalTime;
    }

    public int getMaxReconnectInternalTime() {
        return maxReconnectInternalTime;
    }

    public void setMaxReconnectInternalTime(int maxReconnectInternalTime) {
        this.maxReconnectInternalTime = maxReconnectInternalTime;
    }
}
