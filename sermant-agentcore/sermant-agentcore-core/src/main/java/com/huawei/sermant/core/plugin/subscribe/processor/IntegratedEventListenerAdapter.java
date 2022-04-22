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
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import lombok.Builder;
import lombok.Data;

/**
 * 监听器适配, 多个监听器集成到一个processor处理
 *
 * @author zhouss
 * @since 2022-04-21
 */
@Data
@Builder
public class IntegratedEventListenerAdapter implements DynamicConfigListener {
    private ConfigProcessor processor;

    private String rawGroup;

    @Override
    public void process(DynamicConfigEvent event) {
        if (processor == null) {
            return;
        }
        processor.process(rawGroup, event);
    }
}
