package com.lubanops.apm.plugin.servermonitor.entity;

public class IBMMemoryPool {
    private IBMPoolType type;
    private long init;
    private long max;
    private long committed;
    private long used;

    public IBMMemoryPool(IBMPoolType type) {
        this.type = type;
    }

    public IBMMemoryPool() {
    }

    public IBMPoolType getType() {
        return type;
    }

    public void setType(IBMPoolType type) {
        this.type = type;
    }

    public long getInit() {
        return init;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getCommitted() {
        return committed;
    }

    public void setCommitted(long committed) {
        this.committed = committed;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }
}
