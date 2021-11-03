/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.huawei.gray.dubbo.strategy.TypeStrategy;

/**
 * 空匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class EmptyTypeStrategy extends TypeStrategy {
    @Override
    public String getValue(Object arg, String type) {
        return arg == null ? null : String.valueOf(arg);
    }

    @Override
    public boolean isMatch(String type) {
        return StringUtils.isBlank(type);
    }
}
