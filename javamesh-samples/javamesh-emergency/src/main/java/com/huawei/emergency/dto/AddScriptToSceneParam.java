/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 场景添加脚本的参数
 *
 * @since 2021-10-30
 */
@Data
public class AddScriptToSceneParam {
    @JsonProperty("task_id")
    private int id;

    @JsonProperty("scene_id")
    private int sceneId;

    @NotBlank(message = "{scene.scriptName.notnull}")
    @JsonProperty("script_name")
    private String scriptNameAndUser;

    private String scriptName;

    private String scriptUser;

    @NotBlank(message = "{scene.executionMode.notnull}")
    @JsonProperty("execution_mode")
    private String executionMode;

    @JsonProperty("server_ip")
    private String serverIp;

    @JsonProperty("server_port")
    private String serverPort;

    @JsonProperty("server_user")
    private String serverUser;

    private int executionModeInt;

    private int scriptSequence;
}
