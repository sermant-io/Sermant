/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.exception;

import com.huawei.common.api.CommonErrorCode;

/**
 * 自定义API异常
 *
 * @since 2021-10-30
 */
public class ApiException extends RuntimeException {
    private CommonErrorCode errorCode;

    public ApiException(CommonErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonErrorCode getErrorCode() {
        return errorCode;
    }
}
