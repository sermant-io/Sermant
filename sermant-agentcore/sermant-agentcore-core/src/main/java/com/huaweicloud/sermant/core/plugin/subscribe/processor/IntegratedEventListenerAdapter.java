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

package com.huaweicloud.sermant.core.plugin.subscribe.processor;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 监听器适配, 多个监听器集成到一个processor处理
 *
 * @author zhouss
 * @since 2022-04-21
 */
public class IntegratedEventListenerAdapter implements DynamicConfigListener {
    private final ConfigProcessor processor;

    private final String rawGroup;

    // 订阅时的类加载器
    private final ClassLoader classLoader;

    /**
     * 构造器
     *
     * @param processor 配置处理器
     * @param rawGroup 组标签
     */
    public IntegratedEventListenerAdapter(ConfigProcessor processor, String rawGroup) {
        this.processor = processor;
        this.rawGroup = rawGroup;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (processor == null) {
            return;
        }

        // 订阅时的类加载器与配置监听时的类加载器有可能不是同一个，所以需要还原
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            processor.process(rawGroup, event);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }
}
