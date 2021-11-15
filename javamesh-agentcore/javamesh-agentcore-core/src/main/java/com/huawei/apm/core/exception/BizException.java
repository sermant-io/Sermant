/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.exception;

/**
 * 业务异常
 * 抛出异常到调用方，即抛给用户应用
 *
 * 可参考{@see com.huawei.flowcontrol.exception.FlowControlException}
 *
 * @author zhouss
 * @since 2021-11-12
 */
public class BizException extends RuntimeException{

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }
}
