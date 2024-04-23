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

package io.sermant.router.dubbo.strategy.type;

import io.sermant.core.common.LoggerFactory;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.dubbo.strategy.TypeStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * enabled matching policy
 *
 * @author provenceee
 * @since 2021-10-13
 */
public class IsMethodTypeStrategy extends TypeStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public Optional<String> getValue(Object arg, String type) {
        try {
            Object object = arg.getClass().getMethod(getKey(type)).invoke(arg);
            return object == null ? Optional.empty() : Optional.of(String.valueOf(object));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.warning("Cannot invoke the method, type is " + type);
            return Optional.of(Boolean.FALSE.toString());
        }
    }

    @Override
    public boolean isMatch(String type) {
        return checkType(type);
    }

    @Override
    public String getBeginFlag() {
        return RouterConstant.IS_METHOD_PREFIX;
    }

    @Override
    public String getEndFlag() {
        return RouterConstant.IS_METHOD_SUFFIX;
    }

    @Override
    public String getKey(String type) {
        // The type takes the form of .isXxx(). Cut out the "." and "()" to obtain the method name
        return type.substring(1, type.length() - getEndFlag().length());
    }
}