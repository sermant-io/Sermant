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

package com.huawei.dynamic.config.subscribe;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Locale;

/**
 * 配置监听器
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class ConfigListener implements DynamicConfigListener {
    @Override
    public void process(DynamicConfigEvent event) {
        ConfigHolder.INSTANCE.resolve(event);
        LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
            "[DynamicConfig] Received source [%s], and [%s] it", event.getKey(), event.getEventType()));
        LoggerFactory.getLogger().fine(String.format(Locale.ENGLISH,
            "[DynamicConfig] the value of source is %s", event.getContent()));
    }
}
