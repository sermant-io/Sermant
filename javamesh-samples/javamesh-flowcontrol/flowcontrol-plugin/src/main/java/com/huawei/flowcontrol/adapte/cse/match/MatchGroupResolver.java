/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import com.huawei.flowcontrol.adapte.cse.resolver.AbstractResolver;

/**
 * 业务组
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class MatchGroupResolver extends AbstractResolver<BusinessMatcher> {
    public static final String CONFIG_KEY = "servicecomb.matchGroup";

    public MatchGroupResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<BusinessMatcher> getRuleClass() {
        return BusinessMatcher.class;
    }
}
