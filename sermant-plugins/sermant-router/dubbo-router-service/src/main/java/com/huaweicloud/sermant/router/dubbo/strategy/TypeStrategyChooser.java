/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.strategy;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.dubbo.strategy.type.ArrayTypeStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.type.EmptyTypeStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.type.IsMethodTypeStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.type.ListTypeStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.type.MapTypeStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.type.ObjectTypeStrategy;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * rule policy selector
 *
 * @author provenceee
 * @since 2021-10-13
 */
public enum TypeStrategyChooser {
    /**
     * singleton
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Set<TypeStrategy> typeStrategies;

    TypeStrategyChooser() {
        typeStrategies = new HashSet<>();
        init();
    }

    /**
     * initialize
     */
    private void init() {
        registerTypeStrategy(new ArrayTypeStrategy());
        registerTypeStrategy(new IsMethodTypeStrategy());
        registerTypeStrategy(new ListTypeStrategy());
        registerTypeStrategy(new MapTypeStrategy());
        registerTypeStrategy(new EmptyTypeStrategy());
        registerTypeStrategy(new ObjectTypeStrategy());
    }

    /**
     * policy registration
     *
     * @param typeStrategy expression strategy
     */
    private void registerTypeStrategy(TypeStrategy typeStrategy) {
        typeStrategies.add(typeStrategy);
    }

    /**
     * select policy
     *
     * @param type label rule policy expressions
     * @return Rule strategy
     */
    private Optional<TypeStrategy> choose(String type) {
        for (TypeStrategy typeStrategy : typeStrategies) {
            if (typeStrategy.isMatch(type)) {
                return Optional.of(typeStrategy);
            }
        }
        LOGGER.warning("Cannot found the type strategy, type is " + type);
        return Optional.empty();
    }

    /**
     * Obtain the parameter value based on the policy expression
     *
     * @param type policy expressions
     * @param key parameter index
     * @param arguments array of parameters
     * @return parameter value
     */
    public Optional<String> getValue(String type, String key, Object[] arguments) {
        if (arguments == null) {
            return Optional.empty();
        }
        Optional<TypeStrategy> typeStrategy = choose(type);
        if (!typeStrategy.isPresent()) {
            return Optional.empty();
        }
        int index;
        try {
            index = Integer.parseInt(key.substring(RouterConstant.DUBBO_SOURCE_TYPE_PREFIX.length()));
        } catch (NumberFormatException e) {
            LOGGER.warning("Source type " + key + " is invalid.");
            return Optional.empty();
        }
        if (index < 0 || index >= arguments.length || arguments[index] == null) {
            return Optional.empty();
        }
        return typeStrategy.get().getValue(arguments[index], type);
    }
}