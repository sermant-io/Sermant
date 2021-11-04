/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;

import java.util.List;

/**
 * 列表匹配策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class ListTypeStrategy extends TypeStrategy {
    @Override
    public String getValue(Object arg, String type) {
        if (arg instanceof List) {
            List<?> list = (List<?>) arg;
            int index = Integer.parseInt(getKey(type));
            if (index < 0 || index >= list.size()) {
                return null;
            }
            Object object = list.get(index);
            return object == null ? null : String.valueOf(object);
        }
        return null;
    }

    @Override
    public String getBeginFlag() {
        return ".get(";
    }

    @Override
    public String getEndFlag() {
        return ")";
    }

    @Override
    public boolean checkType(String type) {
        return super.checkType(type) && !type.contains("\"");
    }
}
