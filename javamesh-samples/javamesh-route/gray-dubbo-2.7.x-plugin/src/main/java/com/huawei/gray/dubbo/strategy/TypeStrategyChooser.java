/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.gray.dubbo.strategy.type.ArrayTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.EmptyTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.EnabledTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.ListTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.MapTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.ObjectTypeStrategy;

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

    private static final Logger LOGGER = LogFactory.getLogger();

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
    public TypeStrategy choose(String type) {
        for (TypeStrategy typeStrategy : typeStrategies) {
            if (typeStrategy.isMatch(type)) {
                return typeStrategy;
            }
        }
        LOGGER.warning("Cannot found the type strategy, type is " + type);
        return null;
    }
}
