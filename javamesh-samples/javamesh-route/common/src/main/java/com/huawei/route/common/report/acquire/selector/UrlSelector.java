/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.acquire.selector;

import java.util.List;

/**
 * url选择器
 *
 * @author zhouss
 * @since 2021-11-02
 */
public interface UrlSelector {
    /**
     * url选择器
     *
     * @param urls url列表
     * @return 选择的url
     */
    String select(List<String> urls);
}
