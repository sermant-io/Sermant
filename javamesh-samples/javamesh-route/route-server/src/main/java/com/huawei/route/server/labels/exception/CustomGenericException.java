/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.exception;

/**
 * 自定义异常
 *
 * @author Zhang Hu
 * @since 2021-04-20
 */
public class CustomGenericException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private int code;
    private String errMsg;

    public CustomGenericException(int errCode, String errMsg) {
        super(errMsg);
        this.code = errCode;
        this.errMsg = errMsg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
