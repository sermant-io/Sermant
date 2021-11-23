package com.huawei.apm.backend.entity;

public enum AddressType {
    /**
     * acess服务器的地址
     */
    access;

    public static AddressType getValue(String s) {

        try {
            return AddressType.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
