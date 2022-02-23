/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.adapte.cse.converter;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.collector.PluginCollectorManager;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * yaml格式字符串转换器
 *
 * @param <T> 目标值
 * @author zhouss
 * @since 2021-11-16
 */
public class YamlConverter<T> implements Converter<String, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    protected final Yaml yaml;

    /**
     * 目标类类型
     */
    private final Class<T> targetClass;

    public YamlConverter(Class<T> targetClass) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PluginCollectorManager.class.getClassLoader());
        this.targetClass = targetClass;
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public T convert(String source) {
        if (targetClass == null) {
            return null;
        }
        final ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 此处需使用PluginClassLoader, 需要拿到指定的转换类
            Thread.currentThread().setContextClassLoader(YamlConverter.class.getClassLoader());
            return yaml.loadAs(source, targetClass);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "There were some errors when convert rule, target rule : "
                    + "[%s], source : [%s], error message : [%s]",
                targetClass.getName(), source, ex.getMessage()));
        } finally {
            Thread.currentThread().setContextClassLoader(appClassLoader);
        }
        return null;
    }
}
