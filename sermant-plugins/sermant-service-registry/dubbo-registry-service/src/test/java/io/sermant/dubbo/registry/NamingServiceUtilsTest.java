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

package io.sermant.dubbo.registry;

import io.sermant.dubbo.registry.utils.NamingServiceUtils;
import io.sermant.registry.config.NacosRegisterConfig;
import io.sermant.registry.config.PropertyKeyConst;
import io.sermant.registry.config.RegisterServiceCommonConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Test NamingServiceUtils
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class NamingServiceUtilsTest {
    public static final String ADDRESS = "127.0.0.1:8848";

    /**
     * Test build NamingService
     *
     * @throws NoSuchMethodException Can't find method
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testBuildNacosProperties() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        RegisterServiceCommonConfig commonConfig= new RegisterServiceCommonConfig();
        commonConfig.setAddress("127.0.0.1:8848");
        NacosRegisterConfig registerConfig = new NacosRegisterConfig();
        registerConfig.setGroup("DEFAULT_GROUP");
        registerConfig.setUsername("nacos");
        registerConfig.setPassword("nacos");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("interface", "interface");
        Method method = NamingServiceUtils.class.getDeclaredMethod("buildNacosProperties", Map.class,
            NacosRegisterConfig.class, RegisterServiceCommonConfig.class);
        method.setAccessible(true);
        Properties properties = (Properties)method.invoke(NamingServiceUtils.class, parameters, registerConfig,
                commonConfig);
        Assertions.assertEquals(properties.getProperty(PropertyKeyConst.SERVER_ADDR), ADDRESS);
    }

}
