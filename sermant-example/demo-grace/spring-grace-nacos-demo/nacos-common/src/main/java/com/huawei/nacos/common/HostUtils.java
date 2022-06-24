/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * host工具类
 *
 * @author zhouss
 * @since 2022-03-08
 */
public class HostUtils {
    private static final String LOCAL_IP = "127.0.0.1";

    private static final String LOCAL_HOST = "localhost";

    private static final String EMPTY_STR = "";

    private HostUtils() {
    }

    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     */
    public static String getMachineIp() {
        try {
            for (Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
                networkInterfaceEnumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                String name = networkInterface.getName();
                if (name.contains("docker") || name.contains("lo")) {
                    continue;
                }
                String ip = resolveNetworkIp(networkInterface);
                if (!EMPTY_STR.equals(ip)) {
                    return ip;
                }
            }
        } catch (SocketException ignored) {
            // ignored
        }
        return LOCAL_IP;
    }

    private static String resolveNetworkIp(NetworkInterface networkInterface) {
        for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
            enumIpAddr.hasMoreElements(); ) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            if (!(inetAddress instanceof Inet4Address) || inetAddress.isLoopbackAddress()) {
                continue;
            }
            String ipaddress = inetAddress.getHostAddress();
            if (!LOCAL_IP.equals(ipaddress) && !LOCAL_HOST.equals(ipaddress)) {
                // 取第一个符合要求的IP
                return ipaddress;
            }
        }
        return EMPTY_STR;
    }

    /**
     * 获取域名
     *
     * @return host
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return LOCAL_HOST;
        }
    }
}
