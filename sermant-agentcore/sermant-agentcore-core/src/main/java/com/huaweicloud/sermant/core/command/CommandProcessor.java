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

package com.huaweicloud.sermant.core.command;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CommandProcessor
 *
 * @author zhp
 * @since 2023-09-09
 */
public class CommandProcessor {
    /**
     * COMMAND_EXECUTOR_MAP
     */
    private static final Map<String, CommandExecutor> COMMAND_EXECUTOR_MAP = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    static {
        COMMAND_EXECUTOR_MAP.put(Command.INSTALL_PLUGINS.getValue(), new PluginsInstallCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.UNINSTALL_AGENT.getValue(), new AgentUnInstallCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.UNINSTALL_PLUGINS.getValue(), new PluginsUnInstallCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.CHECK_ENHANCEMENT.getValue(), new CheckEnhancementsCommandExecutor());
    }

    /**
     * constructor
     */
    private CommandProcessor() {
    }

    /**
     * process command
     *
     * @param command command
     */
    public static void process(String command) {
        if (StringUtils.isEmpty(command)) {
            LOGGER.warning("Command information is empty.");
            return;
        }
        LOGGER.log(Level.INFO, "Command information is {0}.", command);
        String[] commandInfo = command.trim().split(":");
        if (commandInfo.length == 0) {
            LOGGER.warning("Illegal command information.");
            return;
        }
        CommandExecutor commandExecutor = COMMAND_EXECUTOR_MAP.get(commandInfo[0].toUpperCase(Locale.ROOT));
        if (commandExecutor == null) {
            LOGGER.warning("No corresponding command executor found.");
            return;
        }
        String commandArgs = commandInfo.length > 1 ? commandInfo[1] : null;
        commandExecutor.execute(commandArgs);
    }
}
