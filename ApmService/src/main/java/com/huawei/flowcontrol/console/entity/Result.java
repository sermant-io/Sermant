package com.huawei.flowcontrol.console.entity;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
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
        return new StringBuilder("Result{")
            .append("success=").append(success)
            .append(", code=").append(code)
            .append(", msg='").append(msg).append('\'')
            .append(", data=").append(data)
            .append('}').toString();
    }
}
