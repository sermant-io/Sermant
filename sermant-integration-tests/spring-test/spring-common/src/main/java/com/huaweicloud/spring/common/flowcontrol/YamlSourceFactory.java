/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.spring.common.flowcontrol;

import com.huaweicloud.spring.common.flowcontrol.utils.EnvUtils;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * yaml配置源工厂
 *
 * @author zhouss
 * @since 2022-07-28
 */
public class YamlSourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        String curName = name;
        if (curName == null) {
            curName = "rule";
        }
        final YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
        Method load = ReflectionUtils.findMethod(YamlPropertySourceLoader.class, "load", String.class,
                Resource.class, String.class);
        Object result;
        if (load != null) {
            // 老版本
            result = ReflectionUtils
                    .invokeMethod(load, yamlPropertySourceLoader, curName, resource.getResource(), null);
        } else {
            load = ReflectionUtils.findMethod(YamlPropertySourceLoader.class, "load", String.class,
                    Resource.class);

            // 新版本
            result = ReflectionUtils
                    .invokeMethod(load, yamlPropertySourceLoader, curName, resource.getResource());
        }
        return format((result instanceof List) ? (PropertySource<?>) ((List<?>) result).get(0)
                : (PropertySource<?>) result);
    }

    private PropertySource<?> format(PropertySource<?> propertySource) {
        final String env = EnvUtils.getEnv(Constants.FLOW_CONTROL_TEST_TARGET_ENV);
        final Object property = propertySource.getProperty(Constants.FLOW_CONTROL_TEST_TARGET_ENV);
        if (env == null || property == null) {
            return propertySource;
        }
        if (!env.equals(property)) {
            return new MapPropertySource("empty", Collections.emptyMap());
        }
        return propertySource;
    }
}
