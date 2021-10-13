package com.lubanops.apm.plugin.servermonitor.entity;

import java.util.List;

/**
 * 服务器监控指标
 */
public class ServerMonitorMetric {

    private final CpuMetric cpu;
    private final List<DiskMetric> disks;
    private final NetworkMetric network;
    private final MemoryMetric memory;
    private final List<IBMMemoryPool> ibmMemoryPools;
    private final long time;

    public ServerMonitorMetric(CpuMetric cpu, List<DiskMetric> disks, NetworkMetric network,
                               MemoryMetric memory, List<IBMMemoryPool> ibmMemoryPools, long time) {
        this.cpu = cpu;
        this.disks = disks;
        this.network = network;
        this.memory = memory;
        this.ibmMemoryPools = ibmMemoryPools;
        this.time = time;
    }

    public CpuMetric getCpu() {
        return cpu;
    }

    public List<DiskMetric> getDisks() {
        return disks;
    }

    public NetworkMetric getNetwork() {
        return network;
    }

    public MemoryMetric getMemory() {
        return memory;
    }

    public List<IBMMemoryPool> getIbmMemoryPools() {
        return ibmMemoryPools;
    }

    public long getTime() {
        return time;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "ServerMonitorMetric{" +
            "cpu=" + cpu +
            ", disks=" + disks +
            ", network=" + network +
            ", memory=" + memory +
            ", ibmMemoryPools=" + ibmMemoryPools +
            ", time=" + time +
            '}';
    }

    public static class Builder {
        private CpuMetric cpu;
        private List<DiskMetric> disks;
        private NetworkMetric network;
        private MemoryMetric memory;
        private List<IBMMemoryPool> ibmMemoryPools;
        private long time;

        public Builder setCpu(CpuMetric cpu) {
            this.cpu = cpu;
            return this;
        }

        public Builder setDisks(List<DiskMetric> disks) {
            this.disks = disks;
            return this;
        }

        public Builder setNetwork(NetworkMetric network) {
            this.network = network;
            return this;
        }

        public Builder setMemory(MemoryMetric memory) {
            this.memory = memory;
            return this;
        }

        public Builder setIbmMemoryPools(List<IBMMemoryPool> ibmMemoryPools) {
            this.ibmMemoryPools = ibmMemoryPools;
            return this;
        }

        public Builder setTime(long time) {
            this.time = time;
            return this;
        }

        public ServerMonitorMetric build() {
            return new ServerMonitorMetric(cpu, disks, network, memory, ibmMemoryPools, time);
        }
    }
}
