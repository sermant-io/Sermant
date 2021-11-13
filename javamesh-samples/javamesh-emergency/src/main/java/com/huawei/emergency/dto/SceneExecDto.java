/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

/**
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
    private Integer taskId;
    private String taskNo;
    private String taskName;
    private String creator;
    private String startTime;
    private String endTime;
    private String status;
    private String statusLabel;
    private String sync;
}
