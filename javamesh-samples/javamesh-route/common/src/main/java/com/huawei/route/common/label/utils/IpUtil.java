/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.label.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * IP工具类
 *
 * @author zhouss
 * @since 2021-11-01
 */
public class IpUtil {
    private static List<String> ipv4List;

    private IpUtil(){}

    /**
     * 获取本机的IPV4地址
     *
     * @return ip
     */
    public static String getIpV4() {
        final List<String> allIpv4 = getAllIpv4();
        if (allIpv4.size() > 0) {
            return allIpv4.get(0);
        } else {
            return "no-hostname";
        }
    }

    /**
     * 获取所有IPV4地址
     *
     * @return ipv4列表
     */
    public static List<String> getAllIpv4() {
        if (ipv4List == null) {
            ipv4List = new LinkedList<String>();
            try {
                Enumeration<NetworkInterface> interfs = NetworkInterface.getNetworkInterfaces();
                while (interfs.hasMoreElements()) {
                    NetworkInterface networkInterface = interfs.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress address = inetAddresses.nextElement();
                        if (address instanceof Inet4Address) {
                            String addressStr = address.getHostAddress();
                            if ("127.0.0.1".equals(addressStr)) {
                                continue;
                            } else if ("localhost".equals(addressStr)) {
                                continue;
                            }
                            ipv4List.add(addressStr);
                        }
                    }
                }
            } catch (SocketException ignored) {
                // ignored
            }
        }
        return ipv4List;
    }

}
