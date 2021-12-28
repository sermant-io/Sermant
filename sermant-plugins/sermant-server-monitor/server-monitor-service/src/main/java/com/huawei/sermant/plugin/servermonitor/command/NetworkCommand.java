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

package com.huawei.sermant.plugin.servermonitor.command;

import com.huawei.sermant.plugin.servermonitor.common.Constant;

import java.io.InputStream;
import java.util.List;

/**
 * 执行命令：cat /proc/net/dev
 * 输出如下内容：
 * Inter-|   Receive                                                |  Transmit
 * face |bytes  packets errs drop fifo frame compressed multicast|bytes packets errs drop fifo colls carrier compressed
 * docker0: 1020039433 5422029 0 0 0   0          0         0 843916915 8849724    0    0    0     0       0          0
 * flannel.1: 418474084655 189175647 0  0 0  0   0     0 590493894734 311142311    0  360    0     0       0          0
 * eth0: 1640055474394 3397136896 0  0  0  0  0      0 2214532164705 2692212634    0    0    0     0       0          0
 * lo: 338601113239 939979857    0  0  0   0    0      0 338601113239 939979857    0    0    0     0       0          0
 * 可以根据提示的进行对应。
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
     * 重构泛PaaS类：com.huawei.sermant.plugin.collection.util.NetWorkParser parse方法
     */
    @Override
    public NetDev parseResult(InputStream inputStream) {
        final List<String> lines = readLines(inputStream);
        if (lines.isEmpty()) {
            return null;
        }
        long receiveBytes = 0L;
        long transmitBytes = 0L;
        long receivePackets = 0L;
        long transmitPackets = 0L;
        for (String line : lines) {
            String[] arr = line.split(STATE_SEPARATOR);
            if (arr.length > 1) {
                String[] netDev = line.split(Constant.REGEX_MULTI_SPACES);
                if (netDev.length < TRANSMIT_PACKETS_INDEX) {
                    continue;
                }
                receiveBytes += Long.parseLong(netDev[RECEIVE_BYTE_INDEX]);
                transmitBytes += Long.parseLong(netDev[TRANSMIT_BYTE_INDEX]);
                receivePackets += Long.parseLong(netDev[RECEIVE_PACKETS_INDEX]);
                transmitPackets += Long.parseLong(netDev[TRANSMIT_PACKETS_INDEX]);
            }
        }
        return new NetDev(receiveBytes, transmitBytes, receivePackets, transmitPackets);
    }

    public static class NetDev {
        private final long receiveBytes;
        private final long receivePackets;
        private final long transmitBytes;
        private final long transmitPackets;

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
