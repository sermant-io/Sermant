/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.configuration;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 配置信息
 *
 * @author Zhang Hu
 * @since 2021-04-15
 */
@Data
public class Configuration implements Serializable {
    private static final long serialVersionUID = -7794124456522077415L;

    @NotBlank(message = "配置名不能为空")
    private String configName;

    @NotBlank(message = "配置描述不能为空")
    private String description;

    @Valid
    @NotEmpty(message = "环境信息不能为空")
    private EnvInfo[] envs;

    @NotBlank(message = "配置值不能为空")
    private String configValue;
}
