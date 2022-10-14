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

package com.huaweicloud.sermant.router.config.handler;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EnabledStrategy;
import com.huaweicloud.sermant.router.config.entity.Strategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 生效策略处理器
 *
 * @author provenceee
 * @since 2022-10-08
 */
public class EnabledStrategyHandler extends AbstractConfigHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String KEY_PREFIX = "sermant.plugin.";

    @Override
    public void handle(DynamicConfigEvent event, String cacheName) {
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy(cacheName);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            LOGGER.info("Enabled strategy is reset.");
            strategy.reset();
            return;
        }
        Map<String, String> map = getEnabledStrategy(event);
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        String strategyValue = map.get("strategy");
        if (strategyValue == null) {
            return;
        }
        Strategy newStrategy;
        try {
            newStrategy = Strategy.valueOf(strategyValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            // 不存在该策略，忽略
            LOGGER.warning(String.format(Locale.ROOT, "Enabled strategy[%s] is invalid.", strategyValue));
            return;
        }
        String value = map.get("value");
        List<String> newValues = Optional.ofNullable(value).map(str -> Arrays.asList(str.split(",")))
            .orElseGet(Collections::emptyList);
        strategy.reset(newStrategy, newValues);
        LOGGER.info(String.format(Locale.ROOT, "Enabled strategy is reset, new strategy is {%s}, value is {%s}.",
            strategyValue, value));
    }

    @Override
    public boolean shouldHandle(String key) {
        return key.startsWith(KEY_PREFIX + "router");
    }

    private Map<String, String> getEnabledStrategy(DynamicConfigEvent event) {
        String content = event.getContent();
        if (StringUtils.isBlank(content)) {
            return Collections.emptyMap();
        }
        return yaml.load(content);
    }
}