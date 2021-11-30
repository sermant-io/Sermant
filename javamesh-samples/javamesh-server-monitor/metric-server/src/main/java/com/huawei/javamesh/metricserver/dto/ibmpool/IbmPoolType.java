/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.ibmpool;

public enum IbmPoolType {
    JCC(0, "JIT code cache"),
    JDC(1, "JIT data cache"),
    TS(2, "tenured-SOA"),
    TL(3, "tenured-LOA"),
    NA(4, "nursery-allocate"),
    NS(5, "nursery-survivor"),
    CS(6, "class storage"),
    MNHS(7, "miscellaneous non-heap storage");

    private final int number;

    private final String description;

    IbmPoolType(int number, String description) {
        this.number = number;
        this.description = description;
    }

    public static IbmPoolType ofNumber(int number) {
        switch (number) {
            case 0:
                return JDC;
            case 1:
                return JCC;
            case 2:
                return TS;
            case 3:
                return TL;
            case 4:
                return NA;
            case 5:
                return NS;
            case 6:
                return CS;
            case 7:
                return MNHS;
            default:
                return null;
        }
    }

    public int getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }
}
