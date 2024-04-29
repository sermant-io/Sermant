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

package io.sermant.core.plugin.agent.enhance;

import io.sermant.core.classloader.ClassLoaderManager;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.service.inject.config.InjectConfig;

import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhance findResource method
 *
 * @author luanwenfei
 * @since 2023-05-08
 */
public class ClassLoaderFindResourceInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Set<String> essentialPackage;

    /**
     * constructor
     */
    public ClassLoaderFindResourceInterceptor() {
        essentialPackage = ConfigManager.getConfig(InjectConfig.class).getEssentialPackage();
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (context.getResult() != null) {
            return context;
        }

        String path = (String) context.getArguments()[0];
        if (isSermantResource(path)) {
            Optional<URL> url = ClassLoaderManager.getPluginClassFinder().findSermantResource(path);
            if (!url.isPresent()) {
                LOGGER.log(Level.WARNING, "Can not find resource [{0}] by sermant.And then find by {1}. ",
                        new Object[]{path, context.getObject()});
            } else {
                context.changeResult(url.get());
                LOGGER.log(Level.INFO, "Find resource: {0} successfully by sermant.", path);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        return context;
    }

    private boolean isSermantResource(String path) {
        String name = path.replace('/', '.');
        for (String prefix : essentialPackage) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
