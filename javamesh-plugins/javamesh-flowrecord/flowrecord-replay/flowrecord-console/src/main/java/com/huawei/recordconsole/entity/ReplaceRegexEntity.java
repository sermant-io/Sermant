/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.recordconsole.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * zookeeper中替换正则表达式对象
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-13
 */
@Getter
@Setter
public class ReplaceRegexEntity {
    /**
     * 替换位置正则表达式
     */
    private String regex;

    /**
     * 替换符号
     */
    private String symbol;
}
