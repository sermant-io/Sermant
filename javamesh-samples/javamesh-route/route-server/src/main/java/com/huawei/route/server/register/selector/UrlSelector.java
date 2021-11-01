/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.selector;

/**
 * 请求地址选择器，针对注册中心多个地址时，通过选择策略匹配请求的地址
 *
 * @author zhouss
 * @since 2021-10-18
 */
public abstract class UrlSelector {
    /**
     * 根据一定规则指定一个url
     *
     * @param urls  url集合
     * @return url
     */
    public String getUrl(String[] urls) {
        if (urls == null || urls.length == 0) {
            throw new IllegalArgumentException("register url didn't configure, please check register url configuration!");
        }
        return getUrlByStrategy(urls);
    }
    /**
     * 根据一定规则指定一个url
     *
     * @param urls  url集合
     * @return url
     */
    abstract String getUrlByStrategy(String[] urls);
}
