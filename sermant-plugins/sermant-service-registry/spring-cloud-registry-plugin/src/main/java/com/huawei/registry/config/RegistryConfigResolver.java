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

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.converter.Converter;
import com.huaweicloud.sermant.core.plugin.converter.YamlConverter;
import com.huaweicloud.sermant.core.plugin.subscribe.processor.OrderConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.utils.MapUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Spring注册相关配置解析
 *
 * @author zhouss
 * @since 2022-05-24
 */
public abstract class RegistryConfigResolver {
    private final Converter<String, Map<String, Object>> yamlConverter = new YamlConverter<>(Map.class);

    /**
     * 更新优雅上下线配置
     *
     * @param event 通知事件
     */
    public void updateConfig(DynamicConfigEvent event) {
        if (!isTargetConfig(event)) {
            return;
        }
        boolean isUpdated;
        if (event instanceof OrderConfigEvent) {
            final Map<String, Object> allData = ((OrderConfigEvent) event).getAllData();
            isUpdated = updateConfig(allData, event.getEventType());
        } else {
            isUpdated = this.updateWithDefaultMode(event);
        }
        if (isUpdated) {
            afterUpdateConfig();
        }
    }

    private boolean updateConfig(Map<String, Object> dic, DynamicConfigEventType eventType) {
        final Object defaultGraceConfig = getDefaultConfig();
        final Object originGraceConfig = getOriginConfig();
        final Field[] declaredFields = originGraceConfig.getClass().getDeclaredFields();
        boolean isUpdated = false;
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            final String name = field.getName();
            final Object configValue = dic.get(getConfigPrefix() + name);
            if (configValue == null || eventType == DynamicConfigEventType.DELETE) {
                // 采用默认值覆盖, 默认值会将会改为修改为第一次从配置和环境变量读取覆盖后的配置值
                final Optional<Object> fieldValue = ReflectUtils.getFieldValue(defaultGraceConfig, name);
                fieldValue.ifPresent(value -> ReflectUtils.setFieldValue(originGraceConfig, name, value));
                isUpdated = true;
                continue;
            }
            isUpdated = true;

            // 若配置中心有该值, 则进行覆盖处理
            ReflectUtils.setFieldValue(originGraceConfig, name, configValue);
        }
        return isUpdated;
    }

    private boolean updateWithDefaultMode(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            return updateConfig(Collections.emptyMap(), event.getEventType());
        } else {
            final Optional<Map<String, Object>> convert = yamlConverter.convert(event.getContent());
            if (convert.isPresent()) {
                final Map<String, Object> data = convert.get();
                final HashMap<String, Object> result = new HashMap<>(data.size());
                MapUtils.resolveNestMap(result, data, null);
                return this.updateConfig(result, event.getEventType());
            }
            return false;
        }
    }

    /**
     * 获取配置前缀
     *
     * @return 配置前缀
     */
    protected abstract String getConfigPrefix();

    /**
     * 获取默认配置类
     *
     * @return 默认配置类
     */
    protected abstract Object getDefaultConfig();

    /**
     * 获取原始配置类, 通过PluginConfigManager获取的配置
     *
     * @return 原始配置类
     */
    protected abstract Object getOriginConfig();

    /**
     * 是否为目标配置
     *
     * @param event 配置监听事件
     * @return 如果为目标配置, 则返回true
     */
    protected abstract boolean isTargetConfig(DynamicConfigEvent event);

    /**
     * 更新配置之后的操作
     */
    protected void afterUpdateConfig() {
    }
}
