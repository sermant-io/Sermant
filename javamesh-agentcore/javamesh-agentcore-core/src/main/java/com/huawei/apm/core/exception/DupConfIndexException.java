/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.exception;

import java.util.Locale;

/**
 * 配置重复索引异常，在使用同一个键解释其值时报出，如{@code config.key=prefix.${config.key}.suffix}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/4
 */
public class DupConfIndexException extends RuntimeException {
    public DupConfIndexException(String key) {
        super(String.format(Locale.ROOT, "Unable to use [%s] to explain [%s]. ", key, key));
    }
}
