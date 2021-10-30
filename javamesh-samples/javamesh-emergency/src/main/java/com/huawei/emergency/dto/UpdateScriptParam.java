/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 更新脚本的实体类
 *
 * @since 2021-10-30
 */
@Data
public class UpdateScriptParam {
    @JsonProperty("script_id")
    private int scriptId;

    @JsonProperty("script_name")
    private String scriptName;

    @JsonProperty("submit_info")
    private String submitInfo;

    private String context;
}
