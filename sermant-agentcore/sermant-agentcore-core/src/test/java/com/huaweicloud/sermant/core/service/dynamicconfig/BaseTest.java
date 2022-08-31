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

package com.huaweicloud.sermant.core.service.dynamicconfig;

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.service.dynamicconfig.config.DynamicConfig;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.config.KieDynamicConfig;
import com.huaweicloud.sermant.core.service.heartbeat.config.HeartbeatConfig;

import org.junit.Assert;
import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * 测试基础化基类
 *
 * @author zhouss
 * @since 2021-12-25
 */
public class BaseTest {
    protected static Map<String, BaseConfig> configManagerMap;

    protected static ClassLoader currentClassLoader;

    @BeforeClass
    public static void init() throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        currentClassLoader = Thread.currentThread().getContextClassLoader();
        final Class<?> aClass = currentClassLoader.loadClass(ConfigManager.class.getName());
        final Field configMap = aClass.getDeclaredField("CONFIG_MAP");
        configMap.setAccessible(true);
        removeFinalModify(configMap);

        configManagerMap = (Map<String, BaseConfig>) configMap.get(null);
        configManagerMap.put("heartbeat", new HeartbeatConfig());
        configManagerMap.put("dynamic.config", new DynamicConfig());
        configManagerMap.put("kie.dynamic.config", new KieDynamicConfig());

        final URL logbackSettingURL = BaseTest.class.getResource("/logback-test.xml");
        Assert.assertNotNull(logbackSettingURL);
        LoggerFactory.init(
                Collections.<String, Object>singletonMap(CommonConstant.LOG_SETTING_FILE_KEY, logbackSettingURL.getPath()));
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
}
