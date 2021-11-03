/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.acquire.selector;

import java.util.List;

/**
 * url选择器
 *
 * @author zhouss
 * @since 2021-11-02
 */
public class UrlSelectorFacade {

    /**
     * url选择器
     * 默认轮询
     *
     * @param urls url列表
     * @return url
     */
    public static String select(List<String> urls) {
        return UrlSelectStrategy.ROUND.getUrlSelector().select(urls);
    }

    /**
     * 根据策略选择url
     *
     * @param urls url列表
     * @param urlSelectStrategy 选择策略
     * @return 选择的url
     */
    public static String select(List<String> urls, UrlSelectStrategy urlSelectStrategy) {
        return urlSelectStrategy.getUrlSelector().select(urls);
    }
}
