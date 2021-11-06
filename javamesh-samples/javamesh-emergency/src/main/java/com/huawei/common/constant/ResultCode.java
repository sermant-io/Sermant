/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.constant;

/**
 * 返回值
 *
 * @since 2021-10-30
 */
public class ResultCode {
    /**
     * 失败
     */
    public static final int FAIL = -1;

    /**
     * 服务器信息为空
     */
    public static final int SERVER_INFO_NULL = -2;

    /**
     * 脚本名已存在
     */
    public static final int SCRIPT_NAME_EXISTS = -3;

    /**
     * 参数异常
     */
    public static final int PARAM_INVALID = -4;

    private ResultCode() {
    }
}
