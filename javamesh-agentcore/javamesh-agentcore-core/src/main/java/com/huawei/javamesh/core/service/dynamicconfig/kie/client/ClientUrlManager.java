/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.service.dynamicconfig.kie.client;

import com.huawei.javamesh.core.service.dynamicconfig.kie.selector.url.UrlSelector;

import java.util.Arrays;
import java.util.List;

/**
 * 客户端请求地址管理器
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class ClientUrlManager {
    private final UrlSelector urlSelector = new UrlSelector();
    private List<String> urls;

    public ClientUrlManager(String urls) {
        resolveUrls(urls);
    }

    /**
     * 客户端请求地址
     *
     * @return url
     */
    public String getUrl() {
        return urlSelector.select(urls);
    }

    /**
     * 解析url
     * 默认多个url使用逗号隔开
     *
     * @param rawUrls url字符串
     */
    public void resolveUrls(String rawUrls) {
        if (rawUrls == null || rawUrls.trim().length() == 0) {
            return;
        }
        urls = Arrays.asList(rawUrls.split(","));
    }
}
