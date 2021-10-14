/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.servermonitor.command;

/**
 * 命令
 */
public class Command {
    public static final CpuCommand CPU = new CpuCommand();
    public static final NetworkCommand NETWORK = new NetworkCommand();
    public static final MemoryCommand MEMORY = new MemoryCommand();
    public static final DiskCommand DISK = new DiskCommand();
}
