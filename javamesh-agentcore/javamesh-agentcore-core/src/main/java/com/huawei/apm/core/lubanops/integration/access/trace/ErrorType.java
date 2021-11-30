package com.huawei.apm.core.lubanops.integration.access.trace;

/**
 * 调用链span错误类型， <br>
 *
 * @author
 * @since 2020年3月4日
 */
public enum ErrorType {

    /**
     * log.error 产生的异常
     */
    log,
    /**
     * 方法抛出的异常
     */
    method,
    /**
     * 状态码异常
     */
    statuscode,
    /**
     * 业务异常
     */
    bizcode,

    /**
     * 第三方传递
     */
    propagate
}
