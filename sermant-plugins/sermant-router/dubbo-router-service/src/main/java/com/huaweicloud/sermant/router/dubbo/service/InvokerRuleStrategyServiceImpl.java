/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.router.common.mapper.AbstractMetadataMapper;
import com.huaweicloud.sermant.router.common.service.InvokerRuleStrategyService;
import com.huaweicloud.sermant.router.dubbo.strategy.RuleStrategyHandler;

/**
 * 设置InvokerRuleStrategy的service
 *
 * @author chengyouling
 * @since 2024-02-23
 */
public class InvokerRuleStrategyServiceImpl implements InvokerRuleStrategyService {
    @Override
    public void builedDubbo3RuleStrategy(AbstractMetadataMapper<Object> mapper) {
        RuleStrategyHandler.INSTANCE.builedDubbo3Mapper(mapper);
    }
}
