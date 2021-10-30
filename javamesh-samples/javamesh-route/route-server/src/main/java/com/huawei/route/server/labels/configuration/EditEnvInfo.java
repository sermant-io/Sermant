/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.configuration;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 编辑某环境的配置使用的对象
 *
 * @author Zhang Hu
 * @since 2021-04-20
 */
@Data
public class EditEnvInfo implements Serializable {
    private static final long serialVersionUID = 6132433892847522087L;

    @NotBlank(message = "配置名不能为空")
    private String configName;

    @NotNull(message = "环境信息不能为空")
    private EnvInfo env;
}
