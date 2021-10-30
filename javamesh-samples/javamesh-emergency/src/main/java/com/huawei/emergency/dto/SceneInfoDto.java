/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 场景信息
 *
 * @since 2021-10-30
 */
@Data
public class SceneInfoDto {
    @JsonProperty("scene_name")
    private String sceneName;

    @JsonProperty("scene_description")
    private String sceneDescription;

    private String status;

    private List<Task> tasks;
}
