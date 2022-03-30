/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.adapte.cse.rule.converter;

import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.List;

/**
 * 定义resilience4j规则转换为sentinel rule
 *
 * @param <S> resilience4j规则
 * @param <R> sentinel规则
 * @author zhouss
 * @since 2022-01-21
 */
public interface RuleConverter<S, R extends Rule> {
    /**
     * 定义resilience4j规则转换为sentinel rule
     *
     * @param resilienceRule resilience4j规则
     * @return 流控规则列表
     */
    List<R> convertToSentinelRule(S resilienceRule);
}
