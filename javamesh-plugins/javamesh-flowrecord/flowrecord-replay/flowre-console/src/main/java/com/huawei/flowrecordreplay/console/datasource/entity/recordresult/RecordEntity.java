/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.recordresult;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 录制流量es存储类
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-06-10
 */
@Getter
@Setter
public class RecordEntity implements Serializable {
    private String jobId;
    private String traceId;
    private String appType;
    private String methodName;
    private String subCallKey;
    private int subCallCount;
    private String requestBody;
    private String requestClass;
    private String responseBody;
    private String responseClass;
    private Date timestamp;
}
