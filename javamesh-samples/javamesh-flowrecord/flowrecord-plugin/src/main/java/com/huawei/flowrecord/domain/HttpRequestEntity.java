package com.huawei.flowrecord.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class HttpRequestEntity {
    /**
     * 请求url
     */
    private String url;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 请求头
     */
    private Map<String, String> headMap;

    /**
     * 请求json字符串
     */
    private Map<String, String[]> httpRequestBody;
}
