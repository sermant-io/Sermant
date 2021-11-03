/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.exception;

/**
 * 流控异常类
 * 拦截器抛出afterMethod方法抛出此类型异常将继续向上抛给调用方
 *
 * @author liyi
 * @since 1.0 2020-10-10
 */
public class FlowControlException extends RuntimeException {
    /**
     * 通用自定义类构造方法
     *
     * @param msg 错误信息
     */
    public FlowControlException(String msg) {
        super(msg);
    }

    /**
     * 通用自定义类构造方法
     *
     * @param e 异常
     */
    public FlowControlException(Throwable e) {
        super(e);
    }

    /**
     * 通用自定义类构造方法
     *
     * @param msg 错误信息
     * @param e 异常
     */
    public FlowControlException(String msg, Throwable e) {
        super(msg, e);
    }

}
