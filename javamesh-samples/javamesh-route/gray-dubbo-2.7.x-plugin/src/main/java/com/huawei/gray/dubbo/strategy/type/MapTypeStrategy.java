/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;

import java.util.Map;

/**
 * map匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class MapTypeStrategy extends TypeStrategy {
    @Override
    public String getValue(Object arg, String type) {
        if (arg instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) arg;
            Object o = map.get(getKey(type));
            return o == null ? null : String.valueOf(o);
        }
        return null;
    }

    @Override
    public boolean isMatch(String type) {
        return checkType(type);
    }

    @Override
    public String getBeginFlag() {
        return ".get(\"";
    }

    @Override
    public String getEndFlag() {
        return "\")";
    }
}
