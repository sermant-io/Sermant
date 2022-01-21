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

import com.huawei.flowcontrol.common.adapte.cse.rule.RetryRule;

import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.Collections;
import java.util.List;

/**
 * 重试规则转换
 *
 * @author zhouss
 * @since 2022-01-21
 */
public class RetryRuleConverter implements RuleConverter<RetryRule, Rule> {
    @Override
    public List<Rule> convertToSentinelRule(RetryRule resilienceRule) {
        return Collections.emptyList();
    }
}
