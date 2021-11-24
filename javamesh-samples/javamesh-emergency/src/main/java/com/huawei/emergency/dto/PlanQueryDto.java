/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

import java.util.List;

/**
 * 用于描述预案查询的显示信息
 *
 * @author y30010171
 * @since 2021-11-10
 **/
@Data
public class PlanQueryDto {
    private Integer key;
    private Integer planId;
    private Integer historyId;
    private String planNo;
    private String planName;
    private String status;
    private String statusLabel;
    private String createTime;
    private String creator;
    private String executeTime;
    private String startTime;
    private String confirm;
    private List<PlanDetailQueryDto> expand;
}
