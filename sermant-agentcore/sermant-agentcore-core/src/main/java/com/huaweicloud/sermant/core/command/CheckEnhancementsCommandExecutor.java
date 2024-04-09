/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.command;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.plugin.PluginManager;
import com.huaweicloud.sermant.core.plugin.agent.info.EnhancementManager;
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.MapUtils;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The command executor for enhancement query
 *
 * @author tangle
 * @since 2023-11-02
 */
public class CheckEnhancementsCommandExecutor implements CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void execute(String args) {
        LOGGER.log(Level.INFO, "---------- PLUGINS ----------");
        for (Map.Entry<String, Plugin> entry : PluginManager.getPluginMap().entrySet()) {
            LOGGER.log(Level.INFO, entry.getKey() + ":" + entry.getValue().getVersion());
        }
        LOGGER.log(Level.INFO, "---------- ENHANCEMENTS ----------");
        Map<String, Map<String, Set<String>>> enhancement = EnhancementManager.getEnhancements();
        if (MapUtils.isEmpty(enhancement)) {
            LOGGER.log(Level.INFO, "No enhancement currently.");
            return;
        }
        for (Map.Entry<String, Map<String, Set<String>>> enhancemantEntry : enhancement.entrySet()) {
            Map<String, Set<String>> methodDescMap = enhancemantEntry.getValue();
            if (MapUtils.isEmpty(methodDescMap)) {
                continue;
            }
            String pluginInfo = enhancemantEntry.getKey();
            LOGGER.log(Level.INFO, pluginInfo);
            for (Map.Entry<String, Set<String>> methodDescEntry : methodDescMap.entrySet()) {
                Set<String> interceptorSet = methodDescEntry.getValue();
                if (CollectionUtils.isEmpty(interceptorSet)) {
                    continue;
                }
                String methodDesc = methodDescEntry.getKey();
                LOGGER.log(Level.INFO, methodDesc + " " + interceptorSet);
            }
        }
    }
}
