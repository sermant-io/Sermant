/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.constant;

import lombok.Getter;

/**
 * 标志位枚举
 *
 * @author y30010171
 * @since 2021-11-27
 **/
@Getter
public enum ValidEnum {
    /**
     * 无效标志
     */
    IN_VALID("0","无效"),
    /**
     * 有效标志
     */
    VALID("1","有效");

    private String value;
    private String description;

    ValidEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
