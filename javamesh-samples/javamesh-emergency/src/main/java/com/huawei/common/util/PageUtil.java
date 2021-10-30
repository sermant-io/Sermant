/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页工具
 *
 * @since 2021-10-30
 */
public class PageUtil {
    private PageUtil() {
    }

    /**
     * 开始分页
     *
     * @param list     原集合
     * @param pageNum  页码
     * @param pageSize 每页多少条数据
     * @param <T>
     * @return 分页后的集合
     **/
    public static <T> List<T> startPage(List<T> list, Integer pageNum, Integer pageSize) {
        if (list == null) {
            return new ArrayList<T>();
        }
        if (list.size() == 0) {
            return new ArrayList<T>();
        }

        int count = list.size(); // 记录总数
        int pageCount = 0; // 页数
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0; // 开始索引
        int toIndex = 0; // 结束索引
        if (pageNum != pageCount) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        return list.subList(fromIndex, toIndex);
    }
}
