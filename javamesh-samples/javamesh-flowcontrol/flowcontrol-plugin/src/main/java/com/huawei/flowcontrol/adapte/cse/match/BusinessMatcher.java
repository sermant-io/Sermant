/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import com.huawei.apm.core.lubanops.integration.enums.HttpMethod;
import com.huawei.flowcontrol.adapte.cse.rule.Configurable;

import java.util.List;
import java.util.Map;

/**
 * 业务场景匹配
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class BusinessMatcher extends Configurable implements Matcher {
    /**
     * 配置名
     */
    private String name;

    /**
     * 该业务场景的所有匹配器
     */
    private List<RequestMatcher> matches;

    @Override
    public boolean isValid() {
        return matches == null || matches.isEmpty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public List<RequestMatcher> getMatches() {
        return matches;
    }

    public void setMatches(List<RequestMatcher> matches) {
        this.matches = matches;
    }

    /**
     * 是否匹配
     *
     * 匹配规则如下:
     * 有一个业务场景匹配，即匹配成功
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param method 请求方法
     * @return 是否匹配
     */
    @Override
    public boolean match(String url, Map<String, String> headers, String method) {
        if (method == null) {
            return false;
        }
        if (matches == null) {
            return false;
        }
        for (RequestMatcher matcher : matches) {
            // 有一个场景匹配成功，则满足要求
            if (matcher.match(url, headers, method)) {
                return true;
            }
        }
        return false;
    }
}
