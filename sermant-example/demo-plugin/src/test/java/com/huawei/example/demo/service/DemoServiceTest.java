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

package com.huawei.example.demo.service;

import com.huawei.example.demo.config.DemoConfig;
import com.huawei.example.demo.config.DemoType;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

/**
 * 插件服务单元测试示例模版
 *
 * @author lilai
 * @version 1.0.0
 * @since 2022-08-25
 */
public class DemoServiceTest {
    private DemoSimpleService service;

    private DemoConfig config;

    private MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * UT执行前
     * 对service中的PluginConfigManager.getPluginConfig()方法进行mock
     */
    @Before
    public void setUp() {
        config = new DemoConfig();
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(DemoConfig.class)).thenReturn(config);
        this.service = new DemoSimpleService();
    }

    /**
     * UT执行后
     * 释放mock对象
     */
    @After
    public void tearDown() {
        mockPluginConfigManager.close();
    }

    /**
     * 对config的具体内容赋值，此处为示例
     */
    public void setConfig() {
        config.setIntField(123456);
        config.setStrField("test");
        config.setEnumType(DemoType.DEMO);
    }


    /**
     * 测试服务启动方法
     */
    @Test
    public void testStart() throws NoSuchFieldException, IllegalAccessException {
        setConfig();
        service.start();

        // 根据插件服务start方法实现的功能来验证服务能力，此处仅为示例，验证config是否正确
        Field configField = service.getClass().getDeclaredField("config");
        configField.setAccessible(true);
        int exactInt = ((DemoConfig) configField.get(service)).getIntField();
        String exactStr = ((DemoConfig) configField.get(service)).getStrField();

        Assert.assertEquals(123456, exactInt);
        Assert.assertEquals("test", exactStr);
    }

    /**
     * 测试服务关闭方法
     */
    @Test
    public void testStop() {
        service.stop();

        // 根据插件服务stop方法实现的功能来验证服务能力，此处仅为示例
        Assert.assertTrue(true);
    }

    /**
     * 测试public方法passiveFunc()
     */
    @Test
    public void testPassiveFunc() {
        service.passiveFunc();

        // 根据插件服务passiveFunc方法实现的功能来验证服务能力，此处仅为示例
        Assert.assertTrue(true);
    }
}