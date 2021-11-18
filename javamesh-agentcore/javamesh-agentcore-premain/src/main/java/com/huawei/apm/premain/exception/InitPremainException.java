/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.exception;

/**
 * 初始化执行premain方法异常
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class InitPremainException extends RuntimeException {
    public InitPremainException(Exception e) {
        super("[" + e.getClass().getSimpleName() + "] " + e.getMessage());
    }
}
