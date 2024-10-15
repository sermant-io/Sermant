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

package io.sermant.dynamic.test.third.plugin.interceptor;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 动态安装卸载测试third插件的拦截器
 *
 * @author tangle
 * @since 2023-09-27
 */
public class RepeatEnhanceInterceptor extends AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext before(ExecuteContext context) {
        try {
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            try (JarFile jarFile = new JarFile(path)) {
                Manifest manifest = jarFile.getManifest();
                Attributes attributes = manifest.getMainAttributes();
                context.getArguments()[Integer.parseInt(attributes.getValue("paramIndex"))] = true;
                LOGGER.log(Level.INFO, "Test repeat enhance, third plugin enhance success");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Test repeat enhance, third plugin enhance failed", e);
            }
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Test repeat enhance, third plugin enhance failed", e);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
