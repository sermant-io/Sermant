package com.lubanops.apm.plugin.servermonitor.command;

public class Command {
    public static final CpuCommand CPU = new CpuCommand();
    public static final NetworkCommand NETWORK = new NetworkCommand();
    public static final MemoryCommand MEMORY = new MemoryCommand();
    public static final DiskCommand DISK = new DiskCommand();
}
