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

package com.huaweicloud.sermant.router.dubbo.strategy.rule;

import com.huaweicloud.sermant.router.common.mapper.AbstractMetadataMapper;
import com.huaweicloud.sermant.router.config.strategy.AbstractRuleStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.instance.MatchInstanceStrategy;
import com.huaweicloud.sermant.router.dubbo.strategy.instance.MismatchInstanceStrategy;

/**
 * 路由规则匹配策略
 *
 * @author provenceee
 * @since 2021-10-14
 */
public class InvokerRuleStrategy extends AbstractRuleStrategy<Object> {
    /**
     * 构造方法
     *
     * @param mapper metadata处理
     */
    public InvokerRuleStrategy(AbstractMetadataMapper<Object> mapper) {
        super("dubbo", new MatchInstanceStrategy(), new MismatchInstanceStrategy(), mapper);
    }
}