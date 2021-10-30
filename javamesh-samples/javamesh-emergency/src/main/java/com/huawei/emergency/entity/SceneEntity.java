/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.sql.Timestamp;

import javax.validation.constraints.NotBlank;

/**
 * 场景实体类
 *
 * @since 2021-10-30
 */
@Data
public class SceneEntity {
    // 场景ID
    @JsonProperty("scene_id")
    private int sceneId;

    // 场景名字
    @NotBlank(message = "{scene.name.notnull}")
    @JsonProperty("scene_name")
    private String sceneName;

    // 场景描述
    @NotBlank(message = "{scene.description.notnull}")
    @JsonProperty("scene_description")
    private String sceneDescription;

    // 创建人
    @JsonProperty("scene_user")
    private String sceneUser;

    // 创建时间
    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

    // 修改时间
    @JsonProperty("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;
}
