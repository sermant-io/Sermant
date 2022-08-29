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

import java.io.InputStream;
import java.util.List;

/**
 * CPU信息命令
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class CpuInfoCommand extends CommonMonitorCommand<CpuInfoCommand.CpuInfoStat> {

    private static final String COMMAND = "lscpu | grep 'Socket(s)' | uniq |  wc -l";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * 重构泛PaaS类：com.huawei.sermant.plugin.collection.util.CpuParser parse方法
     */
    @Override
    public CpuInfoStat parseResult(InputStream inputStream) {
        final List<String> lines = readLines(inputStream);
        int core = 0;
        for (String line : lines) {
            core = Integer.parseInt(line);
        }
        return new CpuInfoStat(core);
    }

    /**
     * CPU信息
     *
     * @since 2022-08-02
     */
    public static class CpuInfoStat {

        /**
         * 核心数
         */
        private final int totalCores;

        /**
         * 构造方法
         *
         * @param totalCores 核心数
         */
        public CpuInfoStat(int totalCores) {
            this.totalCores = totalCores;
        }

        public int getTotalCores() {
            return totalCores;
        }
    }
}
