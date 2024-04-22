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

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Obtain the IP address of the current host
 *
 * @author chengyouling
 * @since 2022-09-29
 */
public class HostIpAddressUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    private HostIpAddressUtils() {

    }

    /**
     * Get the native IP
     *
     * @return Host IP
     * @throws SocketException
     */
    public static String getHostAddress() throws SocketException {
        InetAddress candidateAddress = findNonLoopbackAddress();
        if (candidateAddress != null) {
            return candidateAddress.getHostAddress();
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.warning("get host address error");
        }

        return DEFAULT_ADDRESS;
    }

    private static InetAddress findNonLoopbackAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
        InetAddress candidateAddress = null;
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();
            for (Enumeration<InetAddress> inetAdd = ni.getInetAddresses(); inetAdd.hasMoreElements(); ) {
                InetAddress inetAddress = inetAdd.nextElement();

                // Determine whether it is a loopback address
                if (inetAddress.isLoopbackAddress()) {
                    continue;
                }

                // If it is a site-local address, return it directly
                if (inetAddress.isSiteLocalAddress()) {
                    return inetAddress;
                }
                if (candidateAddress == null) {
                    candidateAddress = inetAddress;
                }
            }
        }
        return candidateAddress;
    }
}
