/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * host工具类
 *
 * @author zhouss
 * @since 2022-03-08
 */
public class HostUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String LOCAL_IP = "127.0.0.1";

    private static final String LOCAL_HOST = "localhost";

    private static final String EMPTY_STR = "";

    private static final int IP_LEN = 4;

    private static final int IP_PARTS_MAX_NUM = 255;

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
        } catch (SocketException exception) {
            LOGGER.warning("An exception occurred while getting the machine's IP address.");
        }
        LOGGER.severe("Can not acquire correct instance ip , it will be replaced by local ip!");
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
            LOGGER.warning("Can not acquire local hostname, it will be replaced by spring register host!");
            return LOCAL_HOST;
        }
    }

    /**
     * 判断是否为同一个实例
     *
     * @param sourceHost 源域名或者IP
     * @param sourcePort 源端口
     * @param targetHost 源域名或者IP
     * @param targetPort 源端口
     * @return 是否为同一个实例
     */
    public static boolean isSameInstance(String sourceHost, int sourcePort, String targetHost, int targetPort) {
        if (sourcePort != targetPort) {
            return false;
        }
        if (sourceHost.equals(targetHost)) {
            return true;
        }
        return isSameMachine(sourceHost, targetHost);
    }

    /**
     * 判断两个host是否为同一个机器
     *
     * @param host 域名或者IP
     * @param targetHost 目标域名或者IP
     * @return 是否属于同一台机器
     */
    public static boolean isSameMachine(String host, String targetHost) {
        final boolean sourceHostIpFlag = isIp(host);
        final boolean targetHostIpFlag = isIp(targetHost);
        if (sourceHostIpFlag == targetHostIpFlag) {
            return host.equals(targetHost);
        }
        if (!sourceHostIpFlag) {
            return compare(targetHost, host);
        }
        return compare(host, targetHost);
    }

    private static boolean compare(String ip, String host) {
        try {
            final InetAddress[] allByName = Inet4Address.getAllByName(host);
            for (InetAddress address : allByName) {
                if (ip.equals(address.getHostAddress())) {
                    return true;
                }
            }
        } catch (UnknownHostException exception) {
            // 若域名解析失败, 则无需再比较, 说明本身域名（或者网络）存在问题, 无需再做比较
            LOGGER.warning("Domain name resolution failure.");
        }
        return false;
    }

    private static boolean isIp(String host) {
        final String[] parts = host.split("\\.");
        if (parts.length != IP_LEN) {
            return false;
        }
        return isIpRegion(parts[0]) && isIpRegion(parts[parts.length - 1]);
    }

    private static boolean isIpRegion(String ipPart) {
        try {
            final int parseInt = Integer.parseInt(ipPart);
            return parseInt >= 0 && parseInt <= IP_PARTS_MAX_NUM;
        } catch (NumberFormatException ignored) {
            // ignored 若非数字, 则表明非IP
            return false;
        }
    }
}
