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

package com.huaweicloud.sermant.config;

import com.huaweicloud.sermant.cache.RuleCache;
import com.huaweicloud.sermant.common.RemovalConstants;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Outlier instance rules
 *
 * @author zhp
 * @since 2023-04-04
 */
public class RemovalDynamicConfigListener implements DynamicConfigListener {
    private static final RemovalConfig CONFIG = PluginConfigManager.getPluginConfig(RemovalConfig.class);

    private final Yaml yaml;

    /**
     * Constructor
     */
    public RemovalDynamicConfigListener() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (!StringUtils.equalsIgnoreCase(RemovalConstants.DYNAMIC_CONFIG_KEY, event.getKey())) {
            return;
        }
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            CONFIG.setEnableRemoval(false);
            return;
        }
        RemovalConfig removalConfig = yaml.loadAs(event.getContent(), RemovalConfig.class);
        if (removalConfig == null) {
            return;
        }
        CONFIG.setEnableRemoval(removalConfig.isEnableRemoval());
        CONFIG.setExpireTime(removalConfig.getExpireTime());
        CONFIG.setExceptions(removalConfig.getExceptions());
        CONFIG.setRecoveryTime(removalConfig.getRecoveryTime());
        CONFIG.setWindowsTime(removalConfig.getWindowsTime());
        CONFIG.setWindowsNum(removalConfig.getWindowsNum());
        RuleCache.updateCache(removalConfig);
    }
}
