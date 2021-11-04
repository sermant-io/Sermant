package com.huawei.apm.core.util;

import java.util.Collection;

/**
 * 参数校验工具
 */
public class Assert {

    public static void notEmpty(Collection<?> collection, String msg) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notEmpty(Object[] objects, String msg) {
        if (objects == null || objects.length == 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void hasText(String text, String msg) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }
}
