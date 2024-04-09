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

/**
 * command
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class Command {

    /**
     * cpu usage command
     */
    public static final CpuCommand CPU = new CpuCommand();

    /**
     * network message command
     */
    public static final NetworkCommand NETWORK = new NetworkCommand();

    /**
     * memory information command
     */
    public static final MemoryCommand MEMORY = new MemoryCommand();

    /**
     * disk information command
     */
    public static final DiskCommand DISK = new DiskCommand();

    /**
     * cpu information command
     */
    public static final CpuInfoCommand CPU_INFO = new CpuInfoCommand();

    /**
     * construction method
     */
    private Command() {
    }
}
