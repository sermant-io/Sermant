/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.config;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.discovery.entity.PlugEffectStategyCache;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.StringUtils;

/**
 * 插件生效规则同步监听器
 *
 * @author chengyouling
 * @since 2022-10-08
 */
public class EffectStategyDynamicConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void process(DynamicConfigEvent event) {
        LOGGER.log(Level.INFO, String.format(Locale.ENGLISH, "Config [%s] has been received, operator type: [%s], content [%s] ",
            event.getKey(), event.getEventType(), event.getContent()));
        if (StringUtils.equalsIgnoreCase(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_LISTENER_KEY, event.getKey())) {
            PlugEffectStategyCache.INSTANCE.resolve(event.getContent());
        }
    }
}
