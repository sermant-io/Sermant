/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 场景明细列表信息
 *
 * @since 2021-10-30
 */
@Data
public class ListDetailsDto {
    @JsonProperty("task_id")
    private int id;

    @JsonProperty("script_name")
    private String scriptNameAndUser;

    private String scriptName;

    private String scriptUser;

    @JsonProperty("submit_info")
    private String submitInfo;

    @JsonProperty("execution_mode")
    private String executionMode;

    @JsonProperty("server_ip")
    private String serverIp;

    @JsonProperty("server_port")
    private String serverPort;

    @JsonProperty("server_user")
    private String serverUser;

    private String status;
}
