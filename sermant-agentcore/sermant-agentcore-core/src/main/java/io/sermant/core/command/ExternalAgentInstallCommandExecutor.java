/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.core.command;

import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.ext.ExternalAgentManager;
import io.sermant.core.utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The command executor of external agent installation
 *
 * @author lilai
 * @since 2024-12-14
 */
public class ExternalAgentInstallCommandExecutor implements CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void execute(String args) {
        Map<String, String> agentArgsMap = DynamicAgentArgsManager.getAgentArgsMap();
        String agentPath = agentArgsMap.get(CommonConstant.AGENT_FILE_KEY);
        if (StringUtils.isEmpty(agentPath)) {
            LOGGER.severe("Failed to install external agent: AGENT_FILE in command args is empty");
            return;
        }

        try {
            ExternalAgentManager.installExternalAgent(true, args, agentPath, agentArgsMap,
                    CommandProcessor.getInstrumentation());
        } catch (IOException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException
                | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Failed to install external agent: {0}. Error message: {1}",
                    new String[]{args, e.getMessage()});
        }
    }
}
