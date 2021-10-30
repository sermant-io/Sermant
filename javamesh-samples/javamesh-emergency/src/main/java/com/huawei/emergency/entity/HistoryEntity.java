/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 任务实体类
 *
 * @since 2021-10-30
 */
@Data
public class HistoryEntity {
    @JsonProperty("history_id")
    private int historyId;

    private int sceneId;

    @JsonProperty("scene_name")
    private String sceneName;

    @JsonProperty("scene_user")
    private String sceneUser;

    @JsonProperty("execute_user_name")
    private String executeUser;

    @JsonProperty("execute_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp executeTime;

    public HistoryEntity() {
    }

    public HistoryEntity(int sceneId, String executeUser, Timestamp executeTime) {
        this.sceneId = sceneId;
        this.executeUser = executeUser;
        this.executeTime = executeTime;
    }
}
