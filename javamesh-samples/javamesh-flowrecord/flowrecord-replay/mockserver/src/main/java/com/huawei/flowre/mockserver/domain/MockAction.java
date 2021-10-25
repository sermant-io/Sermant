/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.domain;

/**
 * mock操作的枚举类 RETURN返回数据结果、SKIP跳过该方法的MOCK、THROWABLE该方法抛出异常
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
public enum MockAction {
    /**
     * 执行操作类型 返回返回数据结果
     */
    RETURN,
    /**
     * 执行操作类型 返回跳过该方法的执行
     */
    SKIP,
    /**
     * 执行操作类型 该方法抛出一个异常
     */
    THROWABLE
}
