/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.exception;

/**
 * 服务初始化异常
 */
public class ServiceInitException extends RuntimeException {
    public ServiceInitException(String message) {
        super(message);
    }

    public ServiceInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
