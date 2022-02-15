/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource.kie.rule.degrade;

import com.huawei.flowcontrol.core.datasource.kie.rule.RuleWrapper;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;

import java.util.List;

/**
 * 降级规则包装类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class DegradeRuleWrapper extends RuleWrapper {
    /**
     * 注册降级规则到降级规则管理器
     *
     * @param dataSource 数据源
     */
    @Override
    public void registerRuleManager(AbstractDataSource<?, ?> dataSource) {
        SentinelProperty property = dataSource.getProperty();
        if (property != null) {
            DegradeRuleManager.register2Property((SentinelProperty<List<DegradeRule>>) property);
        }
    }

    /**
     * 获取规则数据的类信息
     *
     * @return class对象
     */
    @Override
    protected Class<?> getRuleClass() {
        return DegradeRule.class;
    }
}
