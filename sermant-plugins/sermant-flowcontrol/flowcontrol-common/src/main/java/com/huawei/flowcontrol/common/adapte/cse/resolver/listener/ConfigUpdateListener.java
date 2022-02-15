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

package com.huawei.flowcontrol.common.adapte.cse.resolver.listener;

import com.huawei.flowcontrol.common.adapte.cse.rule.Configurable;

import java.util.Map;

/**
 * CSE规则配置更新通知
 *
 * @param <T> 规则实体
 * @author zhouss
 * @since 2021-11-24
 */
public interface ConfigUpdateListener<T extends Configurable> {
    /**
     * 规则配置更新通知
     *
     * @param updateKey 更新键
     * @param rules 所有规则
     */
    void notify(String updateKey, Map<String, T> rules);
}
