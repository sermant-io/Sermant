/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.registry.service.utils;

/**
 * Description: Encapsulates the HTTP client response
 *
 * @author provenceee
 * @since 2022-05-26
 */
public class HttpClientResult {
    /**
     * Response status code
     */
    private final int code;

    /**
     * Response data
     */
    private final String msg;

    /**
     * Constructor
     *
     * @param code Response code
     */
    public HttpClientResult(int code) {
        this(code, null);
    }

    /**
     * Constructor
     *
     * @param code Response code
     * @param msg Response message
     */
    public HttpClientResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}