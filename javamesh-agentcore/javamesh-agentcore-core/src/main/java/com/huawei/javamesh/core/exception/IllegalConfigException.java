/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.exception;

import java.util.Locale;

import com.huawei.javamesh.core.config.common.BaseConfig;

/**
 * 非法配置异常，配置对象缺少默认的构造函数
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/18
 */
public class IllegalConfigException extends RuntimeException {
    public IllegalConfigException(Class<? extends BaseConfig> cls) {
        super(String.format(Locale.ROOT, "Unable to create default instance of %s, please check. ", cls.getName()));
    }
}
