/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import lombok.Data;

import java.util.Date;

/**
 * 预案与任务的拓扑关系
 *
 * @author y30010171
 * @since 2021-11-15
 **/
@Data
public class EmergencyPlanDetail {
    private Integer detailId;

    private Integer planId;

    private Integer sceneId;

    private Integer taskId;

    private Integer preSceneId;

    private Integer preTaskId;

    private Integer parentTaskId;

    private String createUser;

    private Date createTime;

    private String isValid;

    private String sync;
}