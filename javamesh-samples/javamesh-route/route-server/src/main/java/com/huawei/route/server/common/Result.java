/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.common;

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

    public static <R> Result<R> ofSuccess(R data) {
        return Result.<R>builder()
                .success(true)
                .msg("success")
                .data(data)
                .build();
    }

    public static <R> Result<R> ofSuccessMsg(String msg) {
        return Result.<R>builder()
                .success(true)
                .msg(msg)
                .build();
    }

    public static <R> Result<R> ofFail(int code, String msg) {
        return Result.<R>builder()
                .success(false)
                .code(code)
                .msg(msg)
                .build();
    }

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
