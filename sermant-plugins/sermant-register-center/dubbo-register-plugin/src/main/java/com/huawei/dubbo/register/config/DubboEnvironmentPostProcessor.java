/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.dubbo.register.config;

import com.huawei.sermant.core.common.BootArgsIndexer;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.util.JarFileUtil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileUrlResource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 使用配置文件并加装到spring环境中
 *
 * @author provenceee
 * @date 2021/6/2
 */
public class DubboEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String PROPERTY_SOURCE_NAME = "dubbo-sc";

    private static final String PLUGIN_NAME_KEY = "sermant.register.plugin.name";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            String pluginPackageDir = BootArgsIndexer.getPluginPackageDir().getCanonicalPath();
            String pluginName = (String) JarFileUtil.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            String configPath = pluginPackageDir + File.separator + pluginName + File.separator
                    + PluginConstant.CONFIG_DIR_NAME + File.separator + PluginConstant.CONFIG_FILE_NAME;
            List<PropertySource<?>> sources = loader.load(PROPERTY_SOURCE_NAME, new FileUrlResource(configPath));
            environment.getPropertySources().addLast(sources.get(0));
            environment.getSystemProperties().put(PLUGIN_NAME_KEY, pluginName);
        } catch (IOException e) {
            LOGGER.warning("Cannot not find the config.");
        }
    }
}