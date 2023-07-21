/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.subscribe.processor;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 默认配置处理器
 *
 * @author provenceee
 * @since 2023-04-11
 */
public class DefaultConfigProcessor implements ConfigProcessor {
    /**
     * 原监听器
     */
    private final DynamicConfigListener originListener;

    /**
     * 构造器
     *
     * @param listener 原始监听器
     */
    public DefaultConfigProcessor(DynamicConfigListener listener) {
        this.originListener = listener;
    }

    @Override
    public void process(String rawGroup, DynamicConfigEvent event) {
        originListener.process(event);
    }

    @Override
    public void addHolder(ConfigDataHolder dataHolder) {
    }
}