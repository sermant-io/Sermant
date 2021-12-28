/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.gray.dubbo.strategy;

import com.huawei.gray.dubbo.strategy.type.ArrayTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.EmptyTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.EnabledTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.ListTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.MapTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.ObjectTypeStrategy;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.sermant.core.common.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 规则策略选择器
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public enum TypeStrategyChooser {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Set<TypeStrategy> typeStrategies;

    TypeStrategyChooser() {
        typeStrategies = new HashSet<TypeStrategy>();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        registerTypeStrategy(new ArrayTypeStrategy());
        registerTypeStrategy(new EnabledTypeStrategy());
        registerTypeStrategy(new ListTypeStrategy());
        registerTypeStrategy(new MapTypeStrategy());
        registerTypeStrategy(new EmptyTypeStrategy());
        registerTypeStrategy(new ObjectTypeStrategy());
    }

    /**
     * 策略注册
     *
     * @param typeStrategy 表达式策略
     */
    private void registerTypeStrategy(TypeStrategy typeStrategy) {
        typeStrategies.add(typeStrategy);
    }

    /**
     * 选择策略
     *
     * @param type 标签规则策略表达式
     * @return 规则策略
     */
    private TypeStrategy choose(String type) {
        for (TypeStrategy typeStrategy : typeStrategies) {
            if (typeStrategy.isMatch(type)) {
                return typeStrategy;
            }
        }
        LOGGER.warning("Cannot found the type strategy, type is " + type);
        return null;
    }

    /**
     * 根据策略表达式获取参数值
     *
     * @param type 策略表达式
     * @param key 参数索引
     * @param arguments 参数数组
     * @return 参数值
     */
    public String getValue(String type, String key, Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        TypeStrategy typeStrategy = choose(type);
        if (typeStrategy == null) {
            return null;
        }
        int index;
        try {
            index = Integer.parseInt(key.substring(GrayConstant.DUBBO_SOURCE_TYPE_PREFIX.length()));
        } catch (NumberFormatException e) {
            LOGGER.warning("Source type " + key + " is invalid.");
            return null;
        }
        if (index < 0 || index >= arguments.length || arguments[index] == null) {
            return null;
        }
        return typeStrategy.getValue(arguments[index], type);
    }
}
