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

package com.huawei.monitor.command;

import com.huawei.monitor.common.Constants;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * CPU实时信息命令
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
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
     * CPU信息
     *
     * @since 2022-08-02
     */
    public static class CpuStat {

        /**
         * 用户态
         */
        private final long user;

        /**
         * CPU nice信息
         */
        private final long nice;

        /**
         * 系统占用CPU
         */
        private final long system;

        /**
         * CPU空闲情况
         */
        private final long idle;

        /**
         * CPU等待时间
         */
        private final long ioWait;

        /**
         * 构造方法
         *
         * @param user   用户态
         * @param nice   nice信息
         * @param system 系统占用CPU
         * @param idle   CPU空闲情况
         * @param ioWait CPU等待时间
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

        public long getTotal() {
            return system + user + nice + idle + ioWait;
        }
    }
}
