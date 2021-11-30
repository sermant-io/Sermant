/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.domain;

import com.huawei.flowrecordreplay.console.util.Constant;

/**
 * 回放任务请求
 *
 * @author lilai
 * @version 0.0.1
 * @param <R>
 * @since 2021-02-26
 */
public class Result<R> {
    private boolean success;

    private int code;

    private String msg;

    private R data;

    public static <R> Result<R> ofSuccess(R data) {
        return new Result<R>()
                .setSuccess(true)
                .setMsg("success")
                .setData(data);
    }

    public static <R> Result<R> ofSuccessMsg(String msg) {
        return new Result<R>()
                .setSuccess(true)
                .setMsg(msg);
    }

    public static <R> Result<R> ofFail(int code, String msg) {
        Result<R> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <R> Result<R> ofThrowable(int code, Throwable throwable) {
        Result<R> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(throwable.getClass().getName() + ", " + throwable.getMessage());
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Result<R> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public int getCode() {
        return code;
    }

    public Result<R> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<R> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public R getData() {
        return data;
    }

    public Result<R> setData(R data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Result{"
                + "success=" + success
                + ", code=" + code
                + ", msg='" + msg + Constant.SPLIT
                + ", data=" + data
                + '}';
    }
}
