/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 脚本删除参数
 *
 * @since 2021-10-30
 */
@Data
public class ScriptDeleteParam {
    @NotBlank(message = "{script.delete.notnull}")
    Object[] data;
}
