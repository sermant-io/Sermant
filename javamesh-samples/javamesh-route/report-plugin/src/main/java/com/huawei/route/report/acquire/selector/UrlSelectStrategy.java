/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.acquire.selector;

/**
 * url选择策略
 *
 * @author zhouss
 * @since 2021-11-02
 */
public enum UrlSelectStrategy {
    /**
     * 轮询选择
     */
    ROUND(new RoundUrlSelector());

    private UrlSelector urlSelector;

    UrlSelectStrategy(UrlSelector urlSelector) {
        this.urlSelector = urlSelector;
    }

    public UrlSelector getUrlSelector() {
        return urlSelector;
    }
}
