/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.registry.service.utils;

/**
 * Description: 封装httpClient响应结果
 *
 * @author provenceee
 * @since 2022-05-26
 */
public class HttpClientResult {
    /**
     * 响应状态码
     */
    private final int code;

    /**
     * 响应数据
     */
    private final String msg;

    /**
     * 构造方法
     *
     * @param code 响应码
     */
    public HttpClientResult(int code) {
        this(code, null);
    }

    /**
     * 构造方法
     *
     * @param code 响应码
     * @param msg 响应消息
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