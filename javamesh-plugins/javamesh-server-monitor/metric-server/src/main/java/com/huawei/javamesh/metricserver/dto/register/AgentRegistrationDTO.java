/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.register;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AgentRegistrationDTO {
    private final String service;
    private final String serviceInstance;
    private final String jvmVendor;
    private final String jvmVersion;
    private final String runtimeVersion;
    private final List<NetworkAddressDTO> networkAddresses;
}
