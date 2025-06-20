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

package io.sermant.core.utils;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.exception.NetworkInterfacesCheckException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NetworkUtils
 *
 * @author luanwenfei
 * @since 2022-03-19
 */
public class NetworkUtils {
    private static List<String> allNetworkIps = null;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String LOCAL_HOST_IP = "127.0.0.1";

    private static final int IP_OCTET_COUNT = 4;

    private static final int MAX_OCTET_VALUE = 255;

    private static final int MIN_OCTET_VALUE = 0;

    private static final int OCTET_SHIFT_24 = 24;

    private static final int OCTET_SHIFT_16 = 16;

    private static final int OCTET_SHIFT_8 = 8;

    private static final int PART_0 = 0;

    private static final int PART_1 = 1;

    private static final int PART_2 = 2;

    private static final int PART_3 = 3;

    private NetworkUtils() {
    }

    /**
     * Obtain the ip addresses of all network cards of a machine
     *
     * @return List
     */
    public static List<String> getAllNetworkIp() {
        if (allNetworkIps != null) {
            return allNetworkIps;
        }

        List<String> result = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            if (netInterfaces == null) {
                throw new NetworkInterfacesCheckException("netInterfaces is null");
            }

            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> nii = ni.getInetAddresses();
                parseNetworkIp(result, nii);
            }
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "failed to get host ip address", e);
        }

        if (result.size() > 0) {
            allNetworkIps = result;
        }
        return allNetworkIps;
    }

    /**
     * getKubernetesPodIp
     *
     * @return pod ip
     */
    public static String getKubernetesPodIp() {
        String podIp = "";
        try {
            InetAddress ipAddress = InetAddress.getLocalHost();
            podIp = ipAddress.getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.severe("Failed to get IP address of the pod: " + e.getMessage());
        }
        return podIp;
    }

    private static void parseNetworkIp(List<String> result, Enumeration<InetAddress> nii) {
        InetAddress ip;
        while (nii.hasMoreElements()) {
            ip = nii.nextElement();
            if (!ip.getHostAddress().contains(":")) {
                String str = ip.getHostAddress();
                if (!str.startsWith(LOCAL_HOST_IP)) {
                    result.add(str);
                }
            }
        }
    }

    /**
     * Gets all the host names of the machine
     *
     * @return String
     */
    public static Optional<String> getHostName() {
        InetAddress ia;
        try {
            ia = InetAddress.getLocalHost();
            return Optional.ofNullable(ia.getHostName());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    /**
     * Obtain the IP address of Linux
     *
     * @return IP address
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
                if (!StringUtils.EMPTY.equals(ip)) {
                    return ip;
                }
            }
        } catch (SocketException exception) {
            LOGGER.warning("An exception occurred while getting the machine's IP address.");
        }
        LOGGER.severe("Can not acquire correct instance ip , it will be replaced by local ip!");
        return LOCAL_HOST_IP;
    }

    private static String resolveNetworkIp(NetworkInterface networkInterface) {
        for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                enumIpAddr.hasMoreElements(); ) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            if (!(inetAddress instanceof Inet4Address) || inetAddress.isLoopbackAddress()) {
                continue;
            }
            String ipaddress = inetAddress.getHostAddress();
            if (!StringUtils.EMPTY.equals(ipaddress) && !LOCAL_HOST_IP.equals(ipaddress)) {
                // Take the first IP address that meets the requirements
                return ipaddress;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Convert dotted decimal IP address to 32-bit binary string
     *
     * @param ipAddress Dotted decimal IP address
     * @return 32-bit binary IP string, returns empty string for invalid IP
     */
    public static String convertIpToBinaryString(String ipAddress) {
        if (StringUtils.isEmpty(ipAddress)) {
            LOGGER.warning("Input IP address is empty");
            return "";
        }

        try {
            String[] octets = ipAddress.split("\\.");
            if (octets.length != IP_OCTET_COUNT) {
                LOGGER.warning("Invalid IP address format: " + ipAddress);
                return "";
            }

            long[] ipParts = new long[IP_OCTET_COUNT];
            for (int i = 0; i < IP_OCTET_COUNT; ++i) {
                ipParts[i] = Long.parseLong(octets[i]);
                if (ipParts[i] < MIN_OCTET_VALUE || ipParts[i] > MAX_OCTET_VALUE) {
                    LOGGER.warning("Invalid IP octet value: " + ipAddress);
                    return "";
                }
            }

            long ipLongValue = (ipParts[PART_0] << OCTET_SHIFT_24) + (ipParts[PART_1] << OCTET_SHIFT_16)
                    + (ipParts[PART_2] << OCTET_SHIFT_8) + ipParts[PART_3];
            return String.format(Locale.ENGLISH, "%32s", Long.toBinaryString(ipLongValue)).replace(" ", "0");
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid IP address format: " + ipAddress);
            return "";
        }
    }
}
