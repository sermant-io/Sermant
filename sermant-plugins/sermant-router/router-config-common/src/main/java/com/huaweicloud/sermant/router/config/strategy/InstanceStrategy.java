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

package com.huaweicloud.sermant.router.config.strategy;

import java.util.Map;
import java.util.function.Function;

/**
 * 判断实例是否匹配的标签路由策略
 *
 * @param <I> 实例泛型
 * @param <T> 标签泛型
 * @author provenceee
 * @since 2021-12-08
 */
public interface InstanceStrategy<I, T> {
    /**
     * 判断实例是否匹配
     *
     * @param instance 实例
     * @param tags 标签
     * @param mapper 获取metadata的方法
     * @return 是否匹配
     */
    boolean isMatch(I instance, T tags, Function<I, Map<String, String>> mapper);
}