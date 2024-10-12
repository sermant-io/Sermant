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

package io.sermant.router.dubbo.strategy;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.StringUtils;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Rule strategy
 *
 * @author provenceee
 * @since 2021-10-13
 */
public abstract class TypeStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * get the parameter value
     *
     * @param arg parameter
     * @param type how to get the parameters
     * @return parameter value
     */
    public abstract Optional<String> getValue(Object arg, String type);

    /**
     * whether the rule is matched
     *
     * @param type rule expressions
     * @return whether it matches or not
     */
    public boolean isMatch(String type) {
        if (!checkType(type)) {
            return false;
        }
        try {
            Integer.parseInt(getKey(type));
            return true;
        } catch (NumberFormatException e) {
            LOGGER.warning("type " + type + " is not a number.");
            return false;
        }
    }

    /**
     * start string
     *
     * @return start string
     */
    public abstract String getBeginFlag();

    /**
     * end string
     *
     * @return end string
     */
    public abstract String getEndFlag();

    /**
     * check the type of the obtained parameter
     *
     * @param type type
     * @return whether it is legal or not
     */
    public boolean checkType(String type) {
        return StringUtils.isExist(type) && type.startsWith(getBeginFlag()) && type.endsWith(getEndFlag());
    }

    /**
     * obtain the key value of the parameter
     *
     * @param type obtain the parameter type
     * @return Key value
     */
    public String getKey(String type) {
        return type.substring(getBeginFlag().length(), type.length() - getEndFlag().length());
    }
}
