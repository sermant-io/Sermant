/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define.key;

import java.util.HashMap;

/**
 * 不同类型的键匹配条件
 *
 * @author zhouss
 * @since 2021-10-23
 */
public class KeyPair<K, V> extends HashMap<K, V> {
    public KeyPair() {
        super(2);
    }
}
