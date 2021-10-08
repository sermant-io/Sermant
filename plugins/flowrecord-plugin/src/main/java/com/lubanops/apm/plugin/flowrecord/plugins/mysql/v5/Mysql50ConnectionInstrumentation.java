/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

/**
 * {@link Mysql50ConnectionInstrumentation} interceptor the com.mysql.jdbc.Connection class in the 5.0.x version of
 * mysql driver jar.
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class Mysql50ConnectionInstrumentation extends AbstractConnectionInstrumentation {
    private static final String ENHANCE_CLASS = "com.mysql.jdbc.Connection";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }
}
