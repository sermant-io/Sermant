/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.selector;

import java.util.List;

/**
 * 选择器
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface Selector<R> {
    /**
     * 选择
     *
     * @param list 目标集合
     * @return 确定的目标
     */
    R select(List<R> list);

    /**
     * 选择
     *
     * @param strategy 选择策略
     * @param list 目标集合
     * @return 确定的目标
     */
    R select(List<R> list, SelectStrategy<R> strategy);
}
