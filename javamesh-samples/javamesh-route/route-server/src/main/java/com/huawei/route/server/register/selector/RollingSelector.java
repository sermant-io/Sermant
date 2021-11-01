/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.selector;

/**
 * 轮询策略
 *
 * @author zhouss
 * @since 2021-10-18
 */
public class RollingSelector extends UrlSelector{
    private int index = 0;
    @Override
    String getUrlByStrategy(String[] urls) {
        index++;
        int selectIndex = Math.abs(index) % urls.length;
        if (index == Integer.MAX_VALUE) {
            index = 0;
        }
        return urls[selectIndex];
    }
}
