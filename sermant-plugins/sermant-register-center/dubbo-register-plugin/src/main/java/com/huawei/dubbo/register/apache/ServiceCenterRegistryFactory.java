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

package com.huawei.dubbo.register.apache;

import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.utils.ReflectUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * sc注册工厂
 *
 * @author provenceee
 * @since 2021/12/15
 */
public class ServiceCenterRegistryFactory extends AbstractRegistryFactory {
    private static final String APACHE_REGISTRY_CLASS_NAME = "com.huawei.dubbo.register.apache.ServiceCenterRegistry";

    @Override
    protected Registry createRegistry(URL url) {
        // 加载了sc的注册spi的标志
        DubboCache.INSTANCE.loadSc();
        DubboCache.INSTANCE.setUrlClass(url.getClass());
        try {
            Class<?> registryClass = ReflectUtils.defineClass(APACHE_REGISTRY_CLASS_NAME);
            if (registryClass != null) {
                // 由于plugin不能直接new宿主的接口实现类，所以只能手动new出来给宿主
                return (Registry) registryClass.getConstructor(URL.class).newInstance(url);
            }
            return new com.huawei.dubbo.register.apache.ServiceCenterRegistry(url);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException e) {
            return new com.huawei.dubbo.register.apache.ServiceCenterRegistry(url);
        }
    }
}
