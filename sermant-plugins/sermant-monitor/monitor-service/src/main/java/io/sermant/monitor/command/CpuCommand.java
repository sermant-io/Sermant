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

package io.sermant.monitor.command;

import io.sermant.core.common.LoggerFactory;
import io.sermant.monitor.common.Constants;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * cpu real time information command
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class CpuCommand extends CommonMonitorCommand<CpuCommand.CpuStat> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String COMMAND = "cat /proc/stat";

    /**
     * collection line prefix
     */
    private static final String COLLECT_LINE_PREFIX = "cpu ";

    /**
     * index of each status column
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
     * Refactor the pan-PaaS class: io.sermant.plugin.collection.util.CpuParser parse method
     *
     * @param inputStream external process output stream
     * @return the result after parse
     */
    @Override
    public CpuStat parseResult(InputStream inputStream) {
        final List<String> lines = readLines(inputStream);
        for (String line : lines) {
            if (line.startsWith(COLLECT_LINE_PREFIX)) {
                String[] stats = line.split(Constants.REGEX_MULTI_SPACES);
                if (stats.length > IO_WAIT_INDEX) {
                    return new CpuStat(Long.parseLong(stats[USER_INDEX]), Long.parseLong(stats[NICE_INDEX]),
                            Long.parseLong(stats[SYSTEM_INDEX]), Long.parseLong(stats[IDLE_INDEX]),
                            Long.parseLong(stats[IO_WAIT_INDEX]));
                }
            }
        }
        LOGGER.severe("Illegal result.");
        return new CpuStat(0L, 0L, 0L, 0L, 0L);
    }

    /**
     * cpu information
     *
     * @since 2022-08-02
     */
    public static class CpuStat {
        /**
         * user mode
         */
        private final long user;

        /**
         * CPU nice information
         */
        private final long nice;

        /**
         * system cpu usage
         */
        private final long system;

        /**
         * cpu idle condition
         */
        private final long idle;

        /**
         * CPU waiting time
         */
        private final long ioWait;

        /**
         * constructionMethod
         *
         * @param user user mode
         * @param nice nice information
         * @param system system CPU usage
         * @param idle cpu idle condition
         * @param ioWait cpu waiting time
         */
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

        /**
         * obtain the total cpu usage
         *
         * @return sum of cpu usage
         */
        public long getTotal() {
            return system + user + nice + idle + ioWait;
        }
    }
}
