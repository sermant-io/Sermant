/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.core.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;

public class NetworkUtil {
    private static List<String> allNetworkIps = null;

    private final static Logger LOG = LogFactory.getLogger();

    /**
     * 获取一台机器的所有的网卡的ip地址
     * @return
     */
    public static List<String> getAllNetworkIp() {
        if (allNetworkIps != null) {
            return allNetworkIps;
        }

        List<String> result = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            if (netInterfaces == null) {
                throw new RuntimeException("netInterfaces is null");
            }
            InetAddress ip = null;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration<InetAddress> nii = ni.getInetAddresses();
                while (nii.hasMoreElements()) {
                    ip = (InetAddress) nii.nextElement();
                    if (ip.getHostAddress().indexOf(":") == -1) {
                        String s = ip.getHostAddress();
                        if (!s.startsWith("127.0.0.1")) {
                            result.add(s);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            LOG.log(Level.SEVERE, "failed to get host ip address", e);
        }

        if (result.size() > 0) {
            allNetworkIps = result;
        }
        return allNetworkIps;
    }

    /**
     * 获取本机的所有的主机名字
     * @return
     */
    public static String getHostName() {
        InetAddress ia;
        try {
            ia = InetAddress.getLocalHost();
            String host = ia.getHostName();// 获取计算机主机名
            return host;
        } catch (UnknownHostException e) {
            return null;
        }

    }

}
