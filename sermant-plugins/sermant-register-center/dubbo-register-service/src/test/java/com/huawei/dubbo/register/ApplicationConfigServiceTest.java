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

package com.huawei.dubbo.register;

import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.service.ApplicationConfigService;
import com.huawei.dubbo.register.service.ApplicationConfigServiceImpl;

import com.alibaba.dubbo.config.ApplicationConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试ApplicationConfigServiceImpl
 *
 * @author provenceee
 * @since 2022-02-14
 */
public class ApplicationConfigServiceTest {
    private static final String FOO = "foo";

    private final ApplicationConfigService service;

    /**
     * 构造方法
     */
    public ApplicationConfigServiceTest() {
        service = new ApplicationConfigServiceImpl();
    }

    /**
     * 测试Alibaba ApplicationConfig
     *
     * @see com.alibaba.dubbo.config.ApplicationConfig
     */
    @Test
    public void testAlibabaApplicationConfig() {
        // 清空缓存
        DubboCache.INSTANCE.setServiceName(null);
        ApplicationConfig alibabaConfig = new ApplicationConfig();

        // 测试无效应用名
        service.getName(alibabaConfig);
        Assert.assertNull(DubboCache.INSTANCE.getServiceName());

        // 测试有效应用名
        alibabaConfig.setName(FOO);
        service.getName(alibabaConfig);
        Assert.assertEquals(FOO, DubboCache.INSTANCE.getServiceName());
    }

    /**
     * 测试Apache ApplicationConfig
     *
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Test
    public void testApacheApplicationConfig() {
        // 清空缓存
        DubboCache.INSTANCE.setServiceName(null);
        org.apache.dubbo.config.ApplicationConfig apacheConfig = new org.apache.dubbo.config.ApplicationConfig();

        // 测试无效应用名
        service.getName(apacheConfig);
        Assert.assertNull(DubboCache.INSTANCE.getServiceName());

        // 测试有效应用名
        apacheConfig.setName(FOO);
        service.getName(apacheConfig);
        Assert.assertEquals(FOO, DubboCache.INSTANCE.getServiceName());
    }
}