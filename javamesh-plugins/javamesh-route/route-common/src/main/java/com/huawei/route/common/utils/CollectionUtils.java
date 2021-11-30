/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 集合工具类
 *
 * @author pengyuyi
 * @date 2021/11/3
 */
public class CollectionUtils {
    /**
     * 是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 是否为空
     *
     * @param map map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
