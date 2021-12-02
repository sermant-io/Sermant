/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.util;

import java.util.List;

/**
 * 功能描述：ListUtil
 *
 * @author z30009938
 * @since 2021-10-20
 */
public class ListUtils {
    /**
     * 把list转换成指定分隔符分隔的字符串
     *
     * @param list      列表
     * @param separator 分隔符
     * @return list转换成指定分隔符分隔的字符串
     */
    public static String listJoinBySeparator(List<?> list, char separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}
