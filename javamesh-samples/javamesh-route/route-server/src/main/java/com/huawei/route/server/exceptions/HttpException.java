package com.huawei.route.server.exceptions;

/**
 * HTTP请求异常
 *
 * @author zhouss
 * @since 2021-10-08
 */
public class HttpException extends RuntimeException{

    public HttpException(String msg) {
        super(msg);
    }

    public HttpException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
