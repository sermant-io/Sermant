/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor.dto;

public class NetworkAddressDTO {
    private String hostname;
    private String address;

    public String getHostname() {
        return hostname;
    }

    public String getAddress() {
        return address;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
