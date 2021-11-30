/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;

/**
 * 数据匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class ArrayTypeStrategy extends TypeStrategy {
    @Override
    public String getValue(Object arg, String type) {
        if (arg.getClass().isArray()) {
            Object[] arr = (Object[]) arg;
            int index = Integer.parseInt(getKey(type));
            if (index < 0 || index >= arr.length) {
                return null;
            }
            Object object = arr[index];
            return object == null ? null : String.valueOf(object);
        }
        return null;
    }

    @Override
    public String getBeginFlag() {
        return "[";
    }

    @Override
    public String getEndFlag() {
        return "]";
    }
}
