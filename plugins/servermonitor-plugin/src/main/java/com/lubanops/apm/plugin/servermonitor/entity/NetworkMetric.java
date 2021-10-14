package com.lubanops.apm.plugin.servermonitor.entity;

/**
 * network指标
 */
public class NetworkMetric {
    /**
     * 每秒读字节数
     */
    private final long readBytesPerSec;

    /**
     * 每秒写字节数
     */
    private final long writeBytesPerSec;

    /**
     * 每秒读包数
     */
    private final long readPackagesPerSec;

    /**
     * 每秒写包数
     */
    private final long writePackagesPerSec;

    public NetworkMetric() {
        this(0L, 0L, 0L, 0L);
    }

    public NetworkMetric(long readBytesPerSec, long writeBytesPerSec, long readPackagesPerSec, long writePackagesPerSec) {
        this.readBytesPerSec = readBytesPerSec;
        this.writeBytesPerSec = writeBytesPerSec;
        this.readPackagesPerSec = readPackagesPerSec;
        this.writePackagesPerSec = writePackagesPerSec;
    }

    public long getReadBytesPerSec() {
        return readBytesPerSec;
    }

    public long getWriteBytesPerSec() {
        return writeBytesPerSec;
    }

    public long getReadPackagesPerSec() {
        return readPackagesPerSec;
    }

    public long getWritePackagesPerSec() {
        return writePackagesPerSec;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "NetworkMetric{" +
            "readBytesPerSec=" + readBytesPerSec +
            ", writeBytesPerSec=" + writeBytesPerSec +
            ", readPackagesPerSec=" + readPackagesPerSec +
            ", writePackagesPerSec=" + writePackagesPerSec +
            '}';
    }

    public static class Builder {
        private long readBytesPerSec;
        private long writeBytesPerSec;
        private long readPackagesPerSec;
        private long writePackagesPerSec;

        public Builder withReadBytesPerSec(long readBytesPerSec) {
            this.readBytesPerSec = readBytesPerSec;
            return this;
        }

        public Builder withWriteBytesPerSec(long writeBytesPerSec) {
            this.writeBytesPerSec = writeBytesPerSec;
            return this;
        }

        public Builder withReadPackagesPerSec(long readPackagesPerSec) {
            this.readPackagesPerSec = readPackagesPerSec;
            return this;
        }

        public Builder withWritePackagesPerSec(long writePackagesPerSec) {
            this.writePackagesPerSec = writePackagesPerSec;
            return this;
        }

        public NetworkMetric build() {
            return new NetworkMetric(readBytesPerSec, writeBytesPerSec, readPackagesPerSec, writePackagesPerSec);
        }
    }
}
