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

package com.huawei.flowcontrol.core.datasource.kie.rule;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;

/**
 * 规则包装类的抽象类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public abstract class RuleWrapper {
    /**
     * 抽象方法，注册规则数据信息到RuleManager
     *
     * @param dataSource 数据源
     */
    protected abstract void registerRuleManager(AbstractDataSource<?, ?> dataSource);

    /**
     * 抽象方法，获取规则数据的类信息
     *
     * @return 返回class信息
     */
    protected abstract Class<?> getRuleClass();
}
