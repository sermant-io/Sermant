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

package com.huawei.sermant.core.plugin.subscribe.processor;

import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

/**
 * 配置处理
 *
 * @author zhouss
 * @since 2022-04-22
 */
public interface ConfigProcessor {
    /**
     * 事件处理
     *
     * @param rawGroup 原始订阅组
     * @param event    配置事件
     */
    void process(String rawGroup, DynamicConfigEvent event);

    /**
     * 配置持有器， 该配置与标签相对应
     *
     * @param dataHolder 配置持有器
     */
    void addHolder(ConfigDataHolder dataHolder);
}
