/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 用于查询脚本
 *
 * @since 2021-10-30
 */
@Data
public class SearchScriptDto {
    @JsonProperty("script_id")
    private int scriptId;

    @JsonProperty("value")
    private String scriptNameAndUserName;

    @JsonProperty("submit_info")
    private String submitInfo;

    private String context;
}
