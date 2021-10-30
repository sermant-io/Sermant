/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.api;

/**
 * 常用API返回对象接口
 *
 * @author h30009881
 * @since 2021-10-14
 */
public interface CommonErrorCode {
    // 返回码
    long getCode();

    // 返回信息
    String getMessage();
}
