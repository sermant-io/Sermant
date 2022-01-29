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

package com.huawei.sermant.core.agent.interceptor;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.common.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 拦截器链
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public class InterceptorChain {

    @SuppressWarnings("checkstyle:ModifierOrder")
    private final static Logger LOGGER = LoggerFactory.getLogger();

    private static final int NOT_IN_CHAIN = Integer.MAX_VALUE;

    private final Map<String, Integer> priorities = new HashMap<String, Integer>();

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public InterceptorChain(String[] interceptors) {
        int index = 1;
        for (String interceptor : interceptors) {
            final Integer integer = priorities.get(interceptor);
            if (integer == null) {
                priorities.put(interceptor, index++);
            }
        }
        LOGGER.info(String.format("Build interceptor chain {%s} successfully.", Arrays.toString(interceptors)));
    }

    public int getPriority(String interceptor) {
        final Integer priority = priorities.get(interceptor);
        return priority == null ? NOT_IN_CHAIN : priority;
    }
}
