/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

/**
 * 预案查询参数
 *
 * @author y30010171
 * @since 2021-11-12
 **/
@Data
public class PlanQueryParams {
    private String planName;
    private String sceneName;
    private String taskName;
    private String scriptName;
    private String status;
}
