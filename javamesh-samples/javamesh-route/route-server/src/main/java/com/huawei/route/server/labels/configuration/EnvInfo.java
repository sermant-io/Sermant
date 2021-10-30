/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.configuration;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 配置的模式信息
 *
 * @author Zhang Hu
 * @since 2021-04-19
 */
@Data
public class EnvInfo implements Serializable {
    private static final long serialVersionUID = -9119679338607104037L;

    @NotBlank(message = "模式不能为空")
    private String model;

    @NotBlank(message = "模式的值不能为空")
    private String value;
}
