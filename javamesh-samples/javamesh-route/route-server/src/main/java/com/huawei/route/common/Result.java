/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 响应结果
 *
 * @author zhouss
 * @since 2021-10-30
 */
@Getter
@Setter
@Builder
public class Result<R> {
    /**
     * 响应数据
     */
    private R data;

    /**
     * 响应码
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 成功返回结果
     *
     * @param data 响应数据
     * @param <R>  数据类型
     * @return R
     */
    public static <R> Result<R> ofSuccess(R data) {
        return Result.<R>builder()
                .success(true)
                .msg("success")
                .data(data)
                .build();
    }

    /**
     * 无数据成功返回
     *
     * @param msg 提示信息
     * @param <R> 数据类型
     * @return R
     */
    public static <R> Result<R> ofSuccessMsg(String msg) {
        return Result.<R>builder()
                .success(true)
                .msg(msg)
                .build();
    }

    /**
     * 失败返回
     *
     * @param code 返回码
     * @param msg  错误提示信息
     * @param <R>  数据类型
     * @return R
     */
    public static <R> Result<R> ofFail(int code, String msg) {
        return Result.<R>builder()
                .success(false)
                .code(code)
                .msg(msg)
                .build();
    }

    /**
     * 失败返回
     *
     * @param code 响应码
     * @param msg  提示信息
     * @param data 响应数据
     * @param <R>  数据类型
     * @return R
     */
    public static <R> Result<R> ofFail(int code, String msg, R data) {
        return Result.<R>builder()
                .success(false)
                .code(code)
                .data(data)
                .msg(msg)
                .build();
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
