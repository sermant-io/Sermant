/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.selector.url;

import com.huawei.config.selector.SelectStrategy;
import com.huawei.config.selector.Selector;

import java.util.List;

/**
 * url
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class UrlSelector implements Selector<String> {

    private final SelectStrategy<String> defaultStrategy = new SelectStrategy.RoundStrategy<String>();

    @Override
    public String select(List<String> list) {
        return defaultStrategy.select(list);
    }

    @Override
    public String select(List<String> list, SelectStrategy<String> strategy) {
        return strategy.select(list);
    }
}