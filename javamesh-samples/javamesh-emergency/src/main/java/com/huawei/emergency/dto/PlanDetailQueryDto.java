/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

/**
 * 用于描述预案下的任务明细信息
 *
 * @author y30010171
 * @since 2021-11-10
 **/
@Data
public class PlanDetailQueryDto {
    private Integer key;
    private String sceneNo;
    private String sceneName;

    private String scenaNo;
    private String scenaName;

    private String taskNo;
    private String taskName;
    private String subtaskNo;
    private String subtaskName;
    private String channelType;
    private String scriptName;
    private String submitInfo;
}
