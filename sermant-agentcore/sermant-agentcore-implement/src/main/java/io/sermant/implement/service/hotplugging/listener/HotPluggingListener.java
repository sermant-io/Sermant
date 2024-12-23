/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.hotplugging.listener;

import io.sermant.core.command.CommandProcessor;
import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.common.CommonConstant;
import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import io.sermant.core.utils.StringUtils;
import io.sermant.implement.service.hotplugging.entity.HotPluggingConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hot Plugging configuration listener
 *
 * @author zhp
 * @since 2024-08-01
 */
public class HotPluggingListener implements DynamicConfigListener {
    private static final String COMMAND = "command";

    private final YamlConverter yamlConverter = OperationManager.getOperation(YamlConverter.class);

    @Override
    public void process(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE
                || event.getEventType() == DynamicConfigEventType.INIT) {
            return;
        }
        Optional<HotPluggingConfig> configOptional = yamlConverter.convert(event.getContent(),
                HotPluggingConfig.class);
        if (!configOptional.isPresent()) {
            return;
        }
        HotPluggingConfig config = configOptional.get();
        if (StringUtils.isBlank(config.getInstanceIds())
                || !config.getInstanceIds().contains(BootArgsIndexer.getInstanceId())) {
            return;
        }
        Map<String, String> argsMap = new HashMap<>();
        parseParams(config, argsMap);
        argsMap.put(CommonConstant.AGENT_FILE_KEY, config.getAgentPath());
        if (!StringUtils.isEmpty(config.getPluginNames())) {
            argsMap.put(COMMAND, config.getCommandType() + ":" + config.getPluginNames().replace(",", "/"));
        } else if (!StringUtils.isEmpty(config.getExternalAgentName())) {
            argsMap.put(COMMAND, config.getCommandType() + ":" + config.getExternalAgentName());
        } else {
            argsMap.put(COMMAND, config.getCommandType());
        }
        CommandProcessor.process(argsMap);
    }

    /**
     * Parse Parameter Information
     *
     * @param config Hot Plug Configuration
     * @param argsMap Parameter Map
     */
    private static void parseParams(HotPluggingConfig config, Map<String, String> argsMap) {
        String params = config.getParams();
        if (StringUtils.isEmpty(params) || StringUtils.isEmpty(params.trim())) {
            return;
        }
        for (String arg : params.trim().split(",")) {
            final int index = arg.indexOf('=');
            if (index > 0) {
                argsMap.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
            }
        }
    }
}
