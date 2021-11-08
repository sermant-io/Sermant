/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.acquire.selector;

import java.util.List;

/**
 * 轮询选择器
 *
 * @author zhouss
 * @since 2021-11-02
 */
public class RoundUrlSelector implements UrlSelector{
    private int index = 0;

    @Override
    public String select(List<String> urls) {
        index++;
        int selectIndex = Math.abs(index) % urls.size();
        if (index == Integer.MAX_VALUE) {
            index = 0;
        }
        return urls.get(selectIndex);
    }
}
