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

import com.huawei.sermant.core.common.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import static com.huawei.sermant.plugin.servermonitor.common.Constant.REGEX_MULTI_SPACES;

/**
 * 执行指令：cat /proc/stat
 * 输出如下内容：
 * cpu  86468385 641 21175277 1917542581 11613117 0 6898179 0 0 0
 * cpu0 20962641 163 5213893 477119130 2819032 0 4939906 0 0 0
 * cpu1 21746884 149 5318755 480138892 2947198 0 778316 0 0 0
 * cpu2 21900797 147 5328815 480151394 2908898 0 669097 0 0 0
 * cpu3 21858063 182 5313814 480133164 2937987 0 510860 0 0 0
 * intr 12977482518 0 0 752774698 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 519335 0 0 0
 * 1 1 1 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 7 4099652 0 0 0 0 0 0 50 0 145441773 0 2288330244 2 0
 * 26973896 0
 * ctxt 62005215765
 * btime 1613632081
 * processes 47394181
 * procs_running 1
 * procs_blocked 0
 * softirq 6716778875 8 1115785486 216149 3183822034 150181994 0 77618 1166905650 2069518 1097720418
 * 只取第一行的cpu
 */
public class CpuCommand extends CommonMonitorCommand<CpuCommand.CpuStat> {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String COMMAND = "cat /proc/stat";

    /**
     * 采集行前缀
     */
    private static final String COLLECT_LINE_PREFIX = "cpu ";

    /**
     * 各状态列索引
     */
    private static final int USER_INDEX = 1;
    private static final int NICE_INDEX = 2;
    private static final int SYSTEM_INDEX = 3;
    private static final int IDLE_INDEX = 4;
    private static final int IO_WAIT_INDEX = 5;

    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * 重构泛PaaS类：com.huawei.sermant.plugin.collection.util.CpuParser parse方法
     */
    @Override
    public CpuStat parseResult(InputStream inputStream) {
        final List<String> lines = readLines(inputStream);
        for (String line : lines) {
            if (line.startsWith(COLLECT_LINE_PREFIX)) {
                String[] stats = line.split(REGEX_MULTI_SPACES);
                if (stats.length > IO_WAIT_INDEX) {
                    return new CpuStat(Long.parseLong(stats[USER_INDEX]),
                        Long.parseLong(stats[NICE_INDEX]),
                        Long.parseLong(stats[SYSTEM_INDEX]),
                        Long.parseLong(stats[IDLE_INDEX]),
                        Long.parseLong(stats[IO_WAIT_INDEX]));
                }
            }
        }
        LOGGER.severe("Illegal result.");
        return null;
    }

    public static class CpuStat {
        private final long user;
        private final long nice;
        private final long system;
        private final long idle;
        private final long ioWait;

        public CpuStat(long user, long nice, long system, long idle, long ioWait) {
            this.user = user;
            this.nice = nice;
            this.system = system;
            this.idle = idle;
            this.ioWait = ioWait;
        }

        public long getUser() {
            return user;
        }

        public long getNice() {
            return nice;
        }

        public long getSystem() {
            return system;
        }

        public long getIdle() {
            return idle;
        }

        public long getIoWait() {
            return ioWait;
        }

        public long getTotal() {
            return system + user + nice + idle + ioWait;
        }
    }
}
