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

package com.huawei.discovery.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.huaweicloud.sermant.core.common.LoggerFactory;

/**
 * 获取当前主机ip地址
 *
 * @author chengyouling
 * @since 2022-09-29
 */
public class HostIpAddressUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    /**
     * 获取本机ip
     * @return
     * @throws SocketException
     */
    public static String getHostAddress() throws SocketException {
        try {
            Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
            InetAddress candidateAddress = null;
            while (networkInterface.hasMoreElements()) {
                NetworkInterface ni = networkInterface.nextElement();
                for (Enumeration<InetAddress> inetAdd = ni.getInetAddresses(); inetAdd.hasMoreElements();) {
                    InetAddress inetAddress = inetAdd.nextElement();
                    //判断是不是回环地址
                    if (!inetAddress.isLoopbackAddress()) {
                        //如果是site-local地址，直接返回
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        }
                        if (candidateAddress == null) {
                            candidateAddress = inetAddress;
                        }
                    }
                }
            }
            // 如果出去loopback回环地之外无其它地址了，那就InetAddress直接获取
            return candidateAddress == null ? InetAddress.getLocalHost().getHostAddress() : candidateAddress.getHostAddress();
        } catch (Exception e) {
            LOGGER.warning("get host address error");
        }
        return DEFAULT_ADDRESS;
    }
}
