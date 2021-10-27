/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.exception;

/**
 * 功能描述：定义Hercules执行过程中发生的异常
 *
 * @author z30009938
 * @since 2021-10-18
 */
public class HerculesException extends RuntimeException {
    public HerculesException(String message) {
        super(message);
    }
}
