/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.gray.dubbo.strategy.TypeStrategy;
import com.huawei.route.common.gray.constants.GrayConstant;

import java.util.logging.Logger;

/**
 * enabled匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class EnabledTypeStrategy extends TypeStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public String getValue(Object arg, String type) {
        try {
            Object object = arg.getClass().getMethod(getKey(type)).invoke(arg);
            return object == null ? null : String.valueOf(object);
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
