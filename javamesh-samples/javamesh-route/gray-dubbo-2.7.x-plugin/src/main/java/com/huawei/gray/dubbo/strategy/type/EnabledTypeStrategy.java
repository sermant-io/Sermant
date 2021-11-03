/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;
import com.huawei.route.common.constants.GrayConstant;

/**
 * enabled匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class EnabledTypeStrategy extends TypeStrategy {
    @Override
    public String getValue(Object arg, String type) {
        try {
            Object o = arg.getClass().getMethod(getKey(type)).invoke(arg);
            return o == null ? null : String.valueOf(o);
        } catch (Exception e) {
            LOGGER.warning("Cannot invoke the method, type is " + type);
            return Boolean.FALSE.toString();
        }
    }

    @Override
    public boolean isMatch(String type) {
        return GrayConstant.ENABLED_METHOD_NAME.equals(type);
    }

    @Override
    public String getBeginFlag() {
        return ".";
    }

    @Override
    public String getEndFlag() {
        return "()";
    }
}