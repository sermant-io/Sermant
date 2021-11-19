/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.selector;

import java.util.List;
import java.util.Random;

/**
 * 选择策略
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface SelectStrategy<R> {

    /**
     * 选择
     *
     * @param list 目标集合
     * @return 确定的目标
     */
    R select(List<R> list);

    /**
     * 轮询策略
     *
     * @param <R> 集合类型
     */
    class RoundStrategy<R> implements SelectStrategy<R> {
        private int index = 0;

        @Override
        public R select(List<R> list) {
            if (list == null || list.isEmpty()) {
                return null;
            }
            index++;
            int selectIndex = Math.abs(index) % list.size();
            if (index == Integer.MAX_VALUE) {
                index = 0;
            }
            return list.get(selectIndex);
        }
    }

    /**
     * 随机选择策略
     *
     * @param <R> 集合类型
     */
    class RandomStrategy<R> implements SelectStrategy<R> {
        private final Random random = new Random();
        @Override
        public R select(List<R> list) {
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(random.nextInt(list.size()));
        }
    }
}
