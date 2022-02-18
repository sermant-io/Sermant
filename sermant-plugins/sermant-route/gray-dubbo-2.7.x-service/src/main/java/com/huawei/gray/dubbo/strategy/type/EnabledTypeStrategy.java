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

package com.huawei.gray.dubbo.strategy.type;

import com.huawei.gray.dubbo.strategy.TypeStrategy;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.sermant.core.common.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * enabled匹配策略
 *
 * @author provenceee
 * @since 2021/10/13
 */
public class EnabledTypeStrategy extends TypeStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public String getValue(Object arg, String type) {
        try {
            Object object = arg.getClass().getMethod(getKey(type)).invoke(arg);
            return object == null ? null : String.valueOf(object);
        } catch (NoSuchMethodException e) {
            log(type);
            return Boolean.FALSE.toString();
        } catch (IllegalAccessException e) {
            log(type);
            return Boolean.FALSE.toString();
        } catch (InvocationTargetException e) {
            log(type);
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

    private void log(String type) {
        LOGGER.warning("Cannot invoke the method, type is " + type);
    }
}
