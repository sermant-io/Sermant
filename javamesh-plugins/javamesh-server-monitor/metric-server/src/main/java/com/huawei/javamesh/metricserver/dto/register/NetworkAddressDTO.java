/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dto.register;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetworkAddressDTO {
    private final String hostname;
    private final String address;
}
