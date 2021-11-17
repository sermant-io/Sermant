/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import java.util.HashMap;

/**
 * 用于kv格式数据存储
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RawOperator extends HashMap<String, String> {
    /**
     * 默认初始化大小
     */
    private static final int DEFAULT_CAPACITY = 4;

    public RawOperator() {
        super(DEFAULT_CAPACITY);
    }
}
