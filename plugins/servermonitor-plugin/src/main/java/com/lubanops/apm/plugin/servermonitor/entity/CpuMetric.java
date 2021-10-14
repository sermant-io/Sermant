package com.lubanops.apm.plugin.servermonitor.entity;

/**
 * cpu指标
 */
public class CpuMetric {
    /**
     * idle时间百分占比
     */
    private final int idlePercentage;

    /**
     * io wait时间百分占比
     */
    private final int ioWaitPercentage;

    /**
     * sys时间百分占比
     */
    private final int sysPercentage;

    /**
     * user和nice时间百分占比
     */
    private final int userPercentage;

    public CpuMetric() {
        this(1, 0, 0, 0);
    }

    public CpuMetric(int idlePercentage, int ioWaitPercentage, int sysPercentage, int userPercentage) {
        this.idlePercentage = idlePercentage;
        this.ioWaitPercentage = ioWaitPercentage;
        this.sysPercentage = sysPercentage;
        this.userPercentage = userPercentage;
    }

    public int getIdlePercentage() {
        return idlePercentage;
    }

    public int getIoWaitPercentage() {
        return ioWaitPercentage;
    }

    public int getSysPercentage() {
        return sysPercentage;
    }

    public int getUserPercentage() {
        return userPercentage;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "CpuMetric{" +
            "idlePercentage=" + idlePercentage +
            ", ioWaitPercentage=" + ioWaitPercentage +
            ", sysPercentage=" + sysPercentage +
            ", userPercentage=" + userPercentage +
            '}';
    }

    public static class Builder {
        private int idlePercentage;
        private int ioWaitPercentage;
        private int sysPercentage;
        private int userPercentage;

        public Builder withIdlePercentage(int idlePercentage) {
            this.idlePercentage = idlePercentage;
            return this;
        }

        public Builder withIoWaitPercentage(int ioWaitPercentage) {
            this.ioWaitPercentage = ioWaitPercentage;
            return this;
        }

        public Builder withSysPercentage(int sysPercentage) {
            this.sysPercentage = sysPercentage;
            return this;
        }

        public Builder withUserPercentage(int userPercentage) {
            this.userPercentage = userPercentage;
            return this;
        }

        public CpuMetric build() {
            return new CpuMetric(idlePercentage, ioWaitPercentage, sysPercentage, userPercentage);
        }
    }
}
