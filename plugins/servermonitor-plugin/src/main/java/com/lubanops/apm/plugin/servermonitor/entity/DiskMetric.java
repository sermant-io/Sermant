package com.lubanops.apm.plugin.servermonitor.entity;

/**
 * disk指标
 */
public class DiskMetric {
    /**
     * disk名称
     */
    private final String diskName;

    /**
     * 每秒读字节数
     */
    private final long readBytesPerSec;

    /**
     * 每秒写字节数
     */
    private final long writeBytesPerSec;

    /**
     * 采集周期内，IO花费的时间百分比，精度2
     */
    private final double ioSpentPercentage;

    public DiskMetric(String diskName) {
        this(diskName, 0L, 0L, 0.0D);
    }

    public DiskMetric(String diskName, long readBytesPerSec, long writeBytesPerSec, double ioSpentPercentage) {
        this.diskName = diskName;
        this.readBytesPerSec = readBytesPerSec;
        this.writeBytesPerSec = writeBytesPerSec;
        this.ioSpentPercentage = ioSpentPercentage;
    }

    public String getDiskName() {
        return diskName;
    }

    public long getReadBytesPerSec() {
        return readBytesPerSec;
    }

    public long getWriteBytesPerSec() {
        return writeBytesPerSec;
    }

    public double getIoSpentPercentage() {
        return ioSpentPercentage;
    }

    @Override
    public String toString() {
        return "DiskMetric{" +
            "diskName='" + diskName + '\'' +
            ", readBytesPerSec=" + readBytesPerSec +
            ", writeBytesPerSec=" + writeBytesPerSec +
            ", ioSpentPercentage=" + ioSpentPercentage +
            '}';
    }
}
