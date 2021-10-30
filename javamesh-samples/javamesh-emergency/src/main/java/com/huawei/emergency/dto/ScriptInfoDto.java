/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 脚本信息
 *
 * @since 2021-10-30
 */
@Data
public class ScriptInfoDto {
    @JsonProperty("script_name")
    private String scriptName;

    @JsonProperty("submit_info")
    private String submitInfo;

    private String context;

    public ScriptInfoDto(String scriptName,String submitInfo, String context) {
        this.scriptName = scriptName;
        this.submitInfo = submitInfo;
        this.context = context;
    }
}
