/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import com.huawei.flowcontrol.adapte.cse.rule.Configurable;

import java.util.List;

/**
 * 业务场景匹配
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class BusinessMatcher extends Configurable implements Matcher {
    /**
     * 业务名
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
}
