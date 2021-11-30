/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.gray.dubbo.strategy.TypeStrategy;

import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * 实体匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class ObjectTypeStrategy extends TypeStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public String getValue(Object arg, String type) {
        try {
            Field field = arg.getClass().getDeclaredField(getKey(type));
            field.setAccessible(true);
            Object object = field.get(arg);
            return object == null ? null : String.valueOf(object);
        } catch (Exception e) {
            LOGGER.warning("Cannot get the field, type is " + type);
            return null;
        }
    }

    @Override
    public boolean isMatch(String type) {
        return StringUtils.isNotBlank(type) && type.startsWith(".") && !type.contains("(") && !type.contains(")")
                && !type.contains("[") && !type.contains("]");
    }

    @Override
    public String getBeginFlag() {
        return ".";
    }

    @Override
    public String getEndFlag() {
        return "";
    }
}
