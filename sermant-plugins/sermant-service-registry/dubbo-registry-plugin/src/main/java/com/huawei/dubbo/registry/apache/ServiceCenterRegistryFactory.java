/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.registry.apache;

import com.huawei.dubbo.registry.cache.DubboCache;
import com.huawei.dubbo.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SC registered factory
 *
 * @author provenceee
 * @since 2021-12-15
 */
public class ServiceCenterRegistryFactory extends AbstractRegistryFactory {
    private static final String APACHE_REGISTRY_CLASS_NAME = "com.huawei.dubbo.registry.apache.ServiceCenterRegistry";
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected Registry createRegistry(URL url) {
        // Loaded the flag for SC's registered SPI
        DubboCache.INSTANCE.loadSc();
        DubboCache.INSTANCE.setUrlClass(url.getClass());
        try {
            Optional<Class<?>> registryClass = ReflectUtils.defineClass(APACHE_REGISTRY_CLASS_NAME);
            if (registryClass.isPresent()) {
                // Since the plugin cannot directly instantiate the host's interface implementation class,
                // it can only be instantiated manually to the host
                return (Registry) registryClass.get().getConstructor(URL.class).newInstance(url);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "Can not get the registry", e);
        }
        return new com.huawei.dubbo.registry.apache.ServiceCenterRegistry(url);
    }
}
