/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

import java.util.List;

/**
 * @author y30010171
 * @since 2021-11-12
 **/
@Data
public class PlanSaveParams {
    private Integer planId;
    List<TaskNode> expand;
}
