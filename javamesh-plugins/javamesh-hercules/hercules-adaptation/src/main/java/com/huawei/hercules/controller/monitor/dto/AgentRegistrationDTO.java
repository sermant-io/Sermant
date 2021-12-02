/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor.dto;

import java.util.List;

public class AgentRegistrationDTO {
    private String service;
    private String serviceInstance;
    private String jvmVendor;
    private String jvmVersion;
    private String runtimeVersion;
    private List<NetworkAddressDTO> networkAddresses;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(String serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public String getJvmVendor() {
        return jvmVendor;
    }

    public void setJvmVendor(String jvmVendor) {
        this.jvmVendor = jvmVendor;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public List<NetworkAddressDTO> getNetworkAddresses() {
        return networkAddresses;
    }

    public void setNetworkAddresses(List<NetworkAddressDTO> networkAddresses) {
        this.networkAddresses = networkAddresses;
    }
}
