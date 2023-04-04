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

    private String nettyIp;

    private int nettyPort;

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
}
