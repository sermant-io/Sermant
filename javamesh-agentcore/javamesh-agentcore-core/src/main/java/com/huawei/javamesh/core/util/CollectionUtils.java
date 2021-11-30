package com.huawei.javamesh.core.util;

import java.util.Collection;
import java.util.Map;

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