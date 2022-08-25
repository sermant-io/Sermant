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

import java.io.InputStream;
import java.util.List;

/**
 * 内存命令结果解析类
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class MemoryCommand extends CommonMonitorCommand<MemoryCommand.MemInfo> {

    private static final String COMMAND = "cat /proc/meminfo";

    private static final String MEMORY_TOTAL = "MemTotal";
    private static final String MEMORY_FREE = "MemFree";
    private static final String SWAP_CACHED = "SwapCached";
    private static final String BUFFERS = "Buffers";
    private static final String CACHED = "Cached";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * 原泛PaaS类：com.huawei.sermant.plugin.collection.util.MemoryParser parse方法
     */
    @Override
    public MemInfo parseResult(InputStream inputStream) {
        final List<String> lines = readLines(inputStream);

        long memoryTotal = -1L;
        long memoryFree = -1L;
        long buffers = -1L;
        long cached = -1L;
        long swapCached = -1L;

        for (String line : lines) {
            String[] memInfo = line.split(Constants.REGEX_MULTI_SPACES);
            final String category = memInfo[0];
            final long value = Long.parseLong(memInfo[1]);
            if (category.startsWith(MEMORY_TOTAL)) {
                memoryTotal = value;
            } else if (category.startsWith(MEMORY_FREE)) {
                memoryFree = value;
            } else if (category.startsWith(BUFFERS)) {
                buffers = value;
            } else if (category.startsWith(CACHED)) {
                cached = value;
            } else if (category.startsWith(SWAP_CACHED)) {
                swapCached = value;
            }
        }

        if (memoryTotal < 0 || memoryFree < 0 || buffers < 0) {
            return new MemInfo(0L, 0L, 0L, 0L, 0L);
        }

        if (cached < 0 || swapCached < 0) {
            return new MemInfo(0L, 0L, 0L, 0L, 0L);
        }

        return new MemInfo(memoryTotal, memoryFree, buffers, cached, swapCached);
    }

    /**
     * 内存信息
     *
     * @since 2022-08-02
     */
    public static class MemInfo {

        /**
         * 总内存
         */
        private final long memoryTotal;

        /**
         * 空闲内存
         */
        private final long memoryFree;

        /**
         * buffer
         */
        private final long buffers;

        /**
         * 缓存
         */
        private final long cached;

        /**
         * 虚拟缓存
         */
        private final long swapCached;

        /**
         * 构造器
         *
         * @param memoryTotal 总内存
         * @param memoryFree 空闲内存
         * @param buffers buffer缓存
         * @param cached 缓存
         * @param swapCached 虚拟缓存
         */
        public MemInfo(long memoryTotal, long memoryFree, long buffers, long cached, long swapCached) {
            this.memoryTotal = memoryTotal;
            this.memoryFree = memoryFree;
            this.buffers = buffers;
            this.cached = cached;
            this.swapCached = swapCached;
        }

        public long getMemoryTotal() {
            return memoryTotal;
        }

        public long getMemoryFree() {
            return memoryFree;
        }

        public long getBuffers() {
            return buffers;
        }

        public long getCached() {
            return cached;
        }

        public long getSwapCached() {
            return swapCached;
        }
    }
}
