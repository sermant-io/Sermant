/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.util;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 网络地址工具类
 *
 * @author zhp
 * @since 2024-01-15
 */
public class InetAddressUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static volatile String hostAddress;

    private InetAddressUtil() {
    }

    /**
     * 获取本机IP地址
     *
     * @return IP地址
     */
    public static String getHostAddress() {
        if (!StringUtils.isEmpty(hostAddress)) {
            return hostAddress;
        }
        synchronized (InetAddressUtil.class) {
            if (!StringUtils.isEmpty(hostAddress)) {
                return hostAddress;
            }
            try {
                Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
                while (networkInterface.hasMoreElements()) {
                    NetworkInterface ni = networkInterface.nextElement();
                    for (Enumeration<InetAddress> inetAdd = ni.getInetAddresses();
                            inetAdd.hasMoreElements(); ) {
                        InetAddress inetAddress = inetAdd.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isSiteLocalAddress()
                                && inetAddress instanceof Inet4Address) {
                            hostAddress = inetAddress.getHostAddress();
                            return hostAddress;
                        }
                    }
                }
                hostAddress = InetAddress.getLocalHost().getHostAddress();
                return hostAddress;
            } catch (UnknownHostException | SocketException e) {
                LOGGER.log(Level.SEVERE, "Unable to obtain local host address.");
            }
        }
        return hostAddress;
    }

    /**
     * 获取host对应的IP地址
     *
     * @param host 域名
     * @return IP地址
     */
    public static String getHostAddress(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            String address = inetAddress.getHostAddress();
            if (StringUtils.equals(address, "127.0.0.1")) {
                return getHostAddress();
            }
            return address;
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve domain name to IP.");
            return StringUtils.EMPTY;
        }
    }
}
