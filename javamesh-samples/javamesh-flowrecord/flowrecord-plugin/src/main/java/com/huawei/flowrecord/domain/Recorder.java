/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 流量录制入参
 *
 */
@Getter
@Setter
@Builder
public class Recorder implements java.io.Serializable {
    private String jobId;

    private String traceId;

    private String subCallKey;

    private int subCallCount;

    private String appType;

    private String methodName;

    private String requestBody;

    private String requestClass;

    private String responseBody;

    private String responseClass;

    private boolean entry;

    private Date timestamp;
}
