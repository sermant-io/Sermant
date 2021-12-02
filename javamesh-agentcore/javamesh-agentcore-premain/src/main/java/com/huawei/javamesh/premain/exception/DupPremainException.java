/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.premain.exception;

/**
 * 重复执行premain方法异常
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class DupPremainException extends RuntimeException {
    public DupPremainException() {
        super("Unable to execute javamesh agent duplicated. ");
    }
}
