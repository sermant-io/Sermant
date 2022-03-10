/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.config.common.BaseConfig;
import com.huawei.sermant.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.config.DynamicConfig;
import com.huawei.sermant.core.service.heartbeat.HeartbeatConfig;
import com.huawei.sermant.core.service.meta.ServiceMeta;

import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * 测试基础化基类
 *
 * @author zhouss
 * @since 2021-12-25
 */
public class BaseTest {
    /**
     * 统一配置缓存
     */
    protected static Map<String, BaseConfig> configManagerMap;

    /**
     * 当前classloader
     */
    protected static ClassLoader currentClassLoader;

    protected BaseTest() {
    }

    /**
     * 初始化
     *
     * @throws Exception 初始化失败抛出
     */
    @BeforeClass
    public static void init() throws Exception {
        currentClassLoader = Thread.currentThread().getContextClassLoader();
        final Class<?> agentConfigManagerClass = currentClassLoader.loadClass(AgentConfigManager.class.getName());
        final Field nettyServerPort = agentConfigManagerClass.getDeclaredField("backendPort");
        setFieldValue(nettyServerPort, "6888", null);

        final Field nettyServerIp = agentConfigManagerClass.getDeclaredField("backendIp");
        setFieldValue(nettyServerIp, "127.0.0.1", null);

        final Class<?> configManagerClass = currentClassLoader.loadClass(ConfigManager.class.getName());
        final Field configMap = configManagerClass.getDeclaredField("CONFIG_MAP");
        configMap.setAccessible(true);
        removeFinalModify(configMap);
        final Object obj = configMap.get(null);
        if (obj instanceof Map) {
            configManagerMap = (Map<String, BaseConfig>) obj;
            configManagerMap.put("heartbeat", new HeartbeatConfig());
            configManagerMap.put("dynamic.config", new DynamicConfig());
            configManagerMap.put("service.meta", new ServiceMeta());
            ServiceManager.initServices();
            configManagerMap.put("flow.control.plugin", new FlowControlConfig());
        }
    }

    /**
     * 移除final修饰符
     *
     * @param field 字段
     * @throws NoSuchFieldException   无该字段抛出
     * @throws IllegalAccessException 无法拿到该字段抛出
     */
    protected static void removeFinalModify(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    /**
     * 反射设置字段值
     *
     * @param field  字段
     * @param value  值
     * @param target 目前类  静态属性为null
     * @throws IllegalAccessException 找不到抛出异常
     */
    protected static void setFieldValue(Field field, Object value, Object target) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(target, value);
    }
}
