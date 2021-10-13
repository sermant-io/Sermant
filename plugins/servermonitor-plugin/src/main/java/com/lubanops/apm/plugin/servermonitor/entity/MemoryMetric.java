package com.lubanops.apm.plugin.servermonitor.entity;

/**
 * memory指标
 */
public class MemoryMetric {
    /**
     * 总内存大小
     */
    private final long memoryTotal;

    /**
     * 已使用的内存大小
     */
    private final long memoryUsed;

    /**
     * 对应cat /proc/meminfo指令的Buffers
     */
    private final long buffers;

    /**
     * 对应cat /proc/meminfo指令的Cached
     */
    private final long cached;

    /**
     * 对应cat /proc/meminfo指令的SwapCached
     */
    private final long swapCached;

    public MemoryMetric() {
        this(0, 0, 0, 0, 0);
    }

    public MemoryMetric(long memoryTotal, long memoryUsed, long buffers, long cached, long swapCached) {
        this.buffers = buffers;
        this.cached = cached;
        this.memoryTotal = memoryTotal;
        this.swapCached = swapCached;
        this.memoryUsed = memoryUsed;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public long getMemoryUsed() {
        return memoryUsed;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "MemoryMetric{" +
            "memoryTotal=" + memoryTotal +
            ", memoryUsed=" + memoryUsed +
            ", buffers=" + buffers +
            ", cached=" + cached +
            ", swapCached=" + swapCached +
            '}';
    }

    public static class Builder {
        private long memoryTotal;
        private long memoryUsed;
        private long buffers;
        private long cached;
        private long swapCached;

        public Builder withMemoryTotal(long memoryTotal) {
            this.memoryTotal = memoryTotal;
            return this;
        }

        public Builder withMemoryUsed(long memoryUsed) {
            this.memoryUsed = memoryUsed;
            return this;
        }

        public Builder withBuffers(long buffers) {
            this.buffers = buffers;
            return this;
        }

        public Builder withCached(long cached) {
            this.cached = cached;
            return this;
        }

        public Builder withSwapCached(long swapCached) {
            this.swapCached = swapCached;
            return this;
        }

        public MemoryMetric build() {
            return new MemoryMetric(memoryTotal, memoryUsed, buffers, cached, swapCached);
        }
    }

}
