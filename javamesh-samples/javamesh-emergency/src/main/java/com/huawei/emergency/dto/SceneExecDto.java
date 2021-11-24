/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

/**
 * 用于描述场景及任务的执行记录信息
 *
 * @author y30010171
 * @since 2021-11-11
 **/
@Data
public class SceneExecDto {
    private Integer key;
    private Integer execId;
    private Integer planId;
    private Integer sceneId;
    private String sceneNo;
    private String sceneName;
    private Integer scenaId;
    private String scenaNo;
    private String scenaName;
    private Integer taskId;
    private String taskNo;
    private String taskName;
    private String creator;
    private String operator;
    private String startTime;
    private String endTime;
    private String status;
    private String statusLabel;
    private String sync;
}
