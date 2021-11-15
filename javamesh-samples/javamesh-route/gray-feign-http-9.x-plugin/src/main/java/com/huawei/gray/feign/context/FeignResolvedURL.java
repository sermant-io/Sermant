/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.context;

/**
 * feign解析url路径参数前后结果
 *
 * @author lilai
 * @since 2021-11-03
 */
public class FeignResolvedURL {
    /**
     * url before resolved
     */
    private String originUrl;
    /**
     * url after resolved
     */
    private String url;

    public FeignResolvedURL(String originUrl) {
        this.originUrl = originUrl;
    }

    public FeignResolvedURL(String originUrl, String url) {
        this.originUrl = originUrl;
        this.url = url;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
