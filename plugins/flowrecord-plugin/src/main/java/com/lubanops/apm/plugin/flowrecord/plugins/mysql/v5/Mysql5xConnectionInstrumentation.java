/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

/**
 * {@link Mysql5xConnectionInstrumentation } interceptor {@link com.mysql.jdbc.ConnectionImpl} and
 * com.mysql.jdbc.ConnectionImpl in mysql jdbc driver 5.1 and 5.1+
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class Mysql5xConnectionInstrumentation extends AbstractConnectionInstrumentation {
    public static final String ENHANCE_CLASS = "com.mysql.jdbc.ConnectionImpl";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

}
