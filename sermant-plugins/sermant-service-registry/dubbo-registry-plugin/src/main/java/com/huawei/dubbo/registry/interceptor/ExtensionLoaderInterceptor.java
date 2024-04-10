/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.constants.Constant;
import com.huawei.dubbo.registry.utils.ReflectUtils;
import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.config.RegisterType;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enhance the createExtension method of the ExtensionLoader class
 *
 * @author provenceee
 * @since 2022-02-10
 */
public class ExtensionLoaderInterceptor extends AbstractInterceptor {
    private static final String TYPE_FIELD_NAME = "type";

    private static final String APACHE_PREFIX = "apache-";

    private static final String ALIBABA_PREFIX = "alibaba-";

    private static final String APACHE_REGISTRY_FACTORY_CLASS_NAME = "org.apache.dubbo.registry.RegistryFactory";

    private static final String ALIBABA_REGISTRY_FACTORY_CLASS_NAME = "com.alibaba.dubbo.registry.RegistryFactory";

    private static final Map<String, String> REGISTRY_CLASS_NAME = new HashMap<>();

    private static RegisterServiceCommonConfig commonConfig =
            PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);

    static {
        REGISTRY_CLASS_NAME.put("apache-sc", "com.huawei.dubbo.registry.apache.ServiceCenterRegistryFactory");
        REGISTRY_CLASS_NAME.put("alibaba-sc", "com.huawei.dubbo.registry.alibaba.ServiceCenterRegistryFactory");
        REGISTRY_CLASS_NAME.put("apache-nacos", "com.huawei.dubbo.registry.apache.NacosRegistryFactory");
        REGISTRY_CLASS_NAME.put("alibaba-nacos", "com.huawei.dubbo.registry.alibaba.NacosRegistryFactory");
    }

    /**
     * Since plugin cannot directly instantiate the host's SPI implementation class, it can only intercept the host's
     * method of loading SPI and manually instantiate it for the host
     *
     * @param context Execution context
     * @return Execution context
     */
    @Override
    public ExecuteContext before(ExecuteContext context) {
        String name = (String) context.getArguments()[0];
        if (!Constant.SC_REGISTRY_PROTOCOL.equals(name)) {
            // If it is not the SPI of SC/NACOS, return it directly
            return context;
        }
        Class<?> type = (Class<?>) context.getMemberFieldValue(TYPE_FIELD_NAME);
        String typeName = type.getName();
        Optional<Class<?>> factoryClass;

        // Only handle spi of type RegistryFactory
        if (APACHE_REGISTRY_FACTORY_CLASS_NAME.equals(typeName)) {
            factoryClass = ReflectUtils.defineClass(REGISTRY_CLASS_NAME.get(buildKey(APACHE_PREFIX, name)));
        } else if (ALIBABA_REGISTRY_FACTORY_CLASS_NAME.equals(typeName)) {
            factoryClass = ReflectUtils.defineClass(REGISTRY_CLASS_NAME.get(buildKey(ALIBABA_PREFIX, name)));
        } else {
            factoryClass = Optional.empty();
        }
        if (factoryClass.isPresent()) {
            Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());
            cachedClasses.put(name, factoryClass.get());
        }
        return context;
    }

    private String buildKey(String prefix, String name) {
        if (RegisterType.NACOS.equals(commonConfig.getRegisterType())) {
            return prefix + Constant.NACOS_REGISTRY_PROTOCOL;
        }
        return prefix + name;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
