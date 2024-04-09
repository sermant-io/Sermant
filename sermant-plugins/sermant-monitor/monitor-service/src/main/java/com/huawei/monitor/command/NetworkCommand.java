/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.monitor.command;

import com.huawei.monitor.common.Constants;

import java.io.InputStream;
import java.util.List;

/**
 * network command processing interface
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class NetworkCommand extends CommonMonitorCommand<NetworkCommand.NetDev> {

    private static final String COMMAND = "cat /proc/net/dev";

    private static final int RECEIVE_BYTE_INDEX = 2;
    private static final int RECEIVE_PACKETS_INDEX = 3;
    private static final int TRANSMIT_BYTE_INDEX = 10;
    private static final int TRANSMIT_PACKETS_INDEX = 11;

    private static final String STATE_SEPARATOR = ":";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * Refactor the pan-PaaS class: com.huawei.sermant.plugin.collection.util.NetWorkParser parse method
     *
     * @param inputStream external process output stream
     * @return the result after analysis
     */
    @Override
    public NetDev parseResult(InputStream inputStream) {
        final List<String> lines = readLines(inputStream);
        if (lines.isEmpty()) {
            return new NetDev(0L, 0L, 0L, 0L);
        }
        long receiveBytes = 0L;
        long transmitBytes = 0L;
        long receivePackets = 0L;
        long transmitPackets = 0L;
        for (String line : lines) {
            String[] arr = line.split(STATE_SEPARATOR);
            if (arr.length > 1) {
                String[] netDev = line.split(Constants.REGEX_MULTI_SPACES);
                if (netDev.length < TRANSMIT_PACKETS_INDEX) {
                    continue;
                }
                receiveBytes += Long.parseLong(netDev[RECEIVE_BYTE_INDEX]);
                transmitBytes += Long.parseLong(netDev[TRANSMIT_BYTE_INDEX]);
                receivePackets += Long.parseLong(netDev[RECEIVE_PACKETS_INDEX]);
                transmitPackets += Long.parseLong(netDev[TRANSMIT_PACKETS_INDEX]);
            }
        }
        return new NetDev(receiveBytes, receivePackets, transmitBytes, transmitPackets);
    }

    /**
     * network information
     *
     * @since 2022-08-02
     */
    public static class NetDev {

        /**
         * read rate
         */
        private final long receiveBytes;

        /**
         * read packet speed
         */
        private final long receivePackets;

        /**
         * writing speed
         */
        private final long transmitBytes;

        /**
         * write packet speed
         */
        private final long transmitPackets;

        /**
         * constructor
         *
         * @param receiveBytes read rate
         * @param receivePackets writing speed
         * @param transmitBytes packet reading speed
         * @param transmitPackets packet write speed
         */
        public NetDev(long receiveBytes, long receivePackets, long transmitBytes, long transmitPackets) {
            this.receiveBytes = receiveBytes;
            this.receivePackets = receivePackets;
            this.transmitBytes = transmitBytes;
            this.transmitPackets = transmitPackets;
        }

        public long getReceiveBytes() {
            return receiveBytes;
        }

        public long getReceivePackets() {
            return receivePackets;
        }

        public long getTransmitBytes() {
            return transmitBytes;
        }

        public long getTransmitPackets() {
            return transmitPackets;
        }
    }
}
