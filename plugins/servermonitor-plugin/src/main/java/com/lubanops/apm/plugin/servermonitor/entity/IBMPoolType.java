package com.lubanops.apm.plugin.servermonitor.entity;

public enum IBMPoolType {

    IBM_CODE_CACHE_USAGE(0),
    /**
     * <code>IBM_DATA_CACHE_USAGE = 1;</code>
     */
    IBM_DATA_CACHE_USAGE(1),
    /**
     * <code>IBM_TENURED_SOA_USAGE = 2;</code>
     */
    IBM_TENURED_SOA_USAGE(2),
    /**
     * <code>IBM_TENURED_LOA_USAGE = 3;</code>
     */
    IBM_TENURED_LOA_USAGE(3),
    /**
     * <code>IBM_NURSERY_ALLOCATE_USAGE = 4;</code>
     */
    IBM_NURSERY_ALLOCATE_USAGE(4),
    /**
     * <code>IBM_NURSERY_SURVIVOR_USAGE = 5;</code>
     */
    IBM_NURSERY_SURVIVOR_USAGE(5),
    /**
     * <code>IBM_CLASS_STORAGE_USAGE = 6;</code>
     */
    IBM_CLASS_STORAGE_USAGE(6),
    /**
     * <code>IBM_MISCELLANEOUS_USAGE = 7;</code>
     */
    IBM_MISCELLANEOUS_USAGE(7),
    UNRECOGNIZED(-1),
    ;

    private final int value;

    private IBMPoolType(int value) {
        this.value = value;
    }
}
