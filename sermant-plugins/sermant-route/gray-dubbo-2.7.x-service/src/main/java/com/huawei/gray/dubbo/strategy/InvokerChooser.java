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

import com.huawei.gray.dubbo.strategy.invoker.NotMatchVersionsInvokerStrategy;
import com.huawei.gray.dubbo.strategy.invoker.TargetVersionInvokerStrategy;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Invoker匹配策略选择器
 *
 * @author provenceee
 * @since 2021/12/8
 */
public enum InvokerChooser {
    /**
     * 单例
     */
    INSTANCE;

    private static final String NOT_MATCH_STRATEGY_KEY = "notMatchStrategy";

    private static final String TARGET_STRATEGY_KEY = "targetStrategy";

    private final Map<String, InvokerStrategy> map;

    InvokerChooser() {
        map = new HashMap<String, InvokerStrategy>();
        map.put(NOT_MATCH_STRATEGY_KEY, new NotMatchVersionsInvokerStrategy());
        map.put(TARGET_STRATEGY_KEY, new TargetVersionInvokerStrategy());
    }

    /**
     * 选择一个判断invoker是否匹配的策略
     *
     * @param version 目标版本
     * @return 判断invoker是否匹配的策略
     */
    public InvokerStrategy choose(String version) {
        return StringUtils.isBlank(version) ? map.get(NOT_MATCH_STRATEGY_KEY) : map.get(TARGET_STRATEGY_KEY);
    }
}