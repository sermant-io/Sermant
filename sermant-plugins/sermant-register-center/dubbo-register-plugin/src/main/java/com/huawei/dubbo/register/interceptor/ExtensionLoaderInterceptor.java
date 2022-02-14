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

package com.huawei.dubbo.register.interceptor;

import com.huawei.dubbo.register.constants.Constant;
import com.huawei.dubbo.register.utils.ReflectUtils;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import java.util.Map;

/**
 * 增强ExtensionLoader类的createExtension方法
 *
 * @author provenceee
 * @since 2022/2/10
 */
public class ExtensionLoaderInterceptor extends AbstractInterceptor {
    private static final String TYPE_FIELD_NAME = "type";

    private static final String APACHE_REGISTRY_FACTORY_CLASS_NAME = "org.apache.dubbo.registry.RegistryFactory";

    private static final String APACHE_SC_REGISTRY_FACTORY_CLASS_NAME
        = "com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory";

    private static final String ALIBABA_REGISTRY_FACTORY_CLASS_NAME = "com.alibaba.dubbo.registry.RegistryFactory";

    private static final String ALIBABA_SC_REGISTRY_FACTORY_CLASS_NAME
        = "com.huawei.dubbo.register.alibaba.ServiceCenterRegistryFactory";

    /**
     * 由于plugin不能直接new宿主的spi实现类，所以只能拦截宿主加载spi的方法，手动new出来给宿主
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    @Override
    public ExecuteContext before(ExecuteContext context) {
        String name = (String) context.getArguments()[0];
        if (!Constant.SC_REGISTRY_PROTOCOL.equals(name)) {
            // 如果不是sc的spi，直接return
            return context;
        }
        Class<?> type = (Class<?>) context.getMemberFieldValue(TYPE_FIELD_NAME);
        String typeName = type.getName();
        Class<?> factoryClass = null;

        // 只处理类型为RegistryFactory的spi
        if (APACHE_REGISTRY_FACTORY_CLASS_NAME.equals(typeName)) {
            factoryClass = ReflectUtils.defineClass(APACHE_SC_REGISTRY_FACTORY_CLASS_NAME);
        } else if (ALIBABA_REGISTRY_FACTORY_CLASS_NAME.equals(typeName)) {
            factoryClass = ReflectUtils.defineClass(ALIBABA_SC_REGISTRY_FACTORY_CLASS_NAME);
        }
        if (factoryClass != null) {
            Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());
            cachedClasses.put(name, factoryClass);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}