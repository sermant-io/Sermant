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

package io.sermant.core.command;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.event.collector.FrameworkEventCollector;
import io.sermant.core.event.collector.FrameworkEventDefinitions;
import io.sermant.core.plugin.PluginManager;
import io.sermant.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Plugin update command executor
 *
 * @author zhp
 * @since 2024-08-26
 */
public class PluginsUpdateCommandExecutor implements CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void execute(String args) {
        if (StringUtils.isEmpty(args)) {
            LOGGER.log(Level.WARNING, "The argument of command[UPDATE-PLUGINS] is empty.");
            return;
        }
        String[] pluginNames = args.split("/");
        Set<String> pluginSet = Arrays.stream(pluginNames).collect(Collectors.toSet());
        PluginManager.uninstall(pluginSet);
        PluginManager.initPlugins(pluginSet, true);
        FrameworkEventCollector.getInstance().collectdHotPluggingEvent(FrameworkEventDefinitions.SERMANT_PLUGIN_UPDATE,
                "Hot plugging command[UPDATE-PLUGINS] has been processed. Update plugins are "
                        + Arrays.toString(pluginNames));
    }
}
