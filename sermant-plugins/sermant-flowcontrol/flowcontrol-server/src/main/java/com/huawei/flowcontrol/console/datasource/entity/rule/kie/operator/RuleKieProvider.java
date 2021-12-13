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

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

/**
 * kie配置中心规则查询接口层
 *
 * @param <E> 不同规则的entity
 * @author Sherlockhan
 * @since 2020-12-21
 */
public interface RuleKieProvider<E> {
    /**
     * 配置标签的最大长度
     */
    int MAX_LENGTH_OF_KIE_LABEL = 31;

    E getRules(String app) throws Exception;
}
