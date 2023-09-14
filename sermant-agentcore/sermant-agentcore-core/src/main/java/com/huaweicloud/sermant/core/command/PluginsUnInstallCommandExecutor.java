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
import com.huaweicloud.sermant.core.plugin.PluginManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 插件卸载命令执行器
 *
 * @author zhp
 * @since 2023-09-09
 */
public class PluginsUnInstallCommandExecutor implements CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void execute(String args) {
        if (StringUtils.isEmpty(args)) {
            LOGGER.log(Level.WARNING, "The argument of command[UNINSTALL-PLUGINS] is empty.");
            return;
        }
        String[] pluginNames = args.split("\\|");
        PluginManager.uninstall(Arrays.stream(pluginNames).collect(Collectors.toSet()));
    }
}
