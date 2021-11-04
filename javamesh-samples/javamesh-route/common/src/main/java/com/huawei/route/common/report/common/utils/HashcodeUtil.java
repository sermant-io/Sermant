/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.common.utils;

/**
 * hashcode生成工具类
 *
 * @author wl
 * @since 2021-06-11
 */
public class HashcodeUtil {
    private HashcodeUtil() {
    }

    /**
     * 使用FNV1_32_HASH算法计算hash值
     *
     * @param str 待求hashcode的字符串
     * @return long
     */
    public static Long FNV1_32_HASH(String str) {
        final long p = 1677769L;

        long hash = 0L;
        long len = str.length();
        for (int i = 0; i < len; i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
