/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.implement.service.dynamicconfig.nacos;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Nacos动态配置订阅功能测试
 *
 * @author tangle
 * @since 2023-09-08
 */
public class DynamicConfigSubscribeTest extends NacosBaseTest {
    /**
     * 订阅器
     */
    DynamicConfigSubscribe dynamicConfigSubscribe;

    public DynamicConfigSubscribeTest() {
    }

    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        try {
            // 测试订阅
            TestListener testListener = new TestListener();
            dynamicConfigSubscribe = new DynamicConfigSubscribe("testServiceName", testListener, "testPluginName",
                    "testSubscribeKey");
            Assert.assertTrue(dynamicConfigSubscribe.subscribe());
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment",
                            "content:1"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment_service:testServiceName",
                            "content:2"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "testCustomLabel:testCustomLabelValue",
                            "content:3"));
            Thread.sleep(1000);
            Assert.assertTrue(testListener.isChange());
            Field fieldListener = nacosDynamicConfigService.getClass().getDeclaredField("listeners");
            fieldListener.setAccessible(true);
            Assert.assertEquals(3,
                    ((List<NacosListener>) fieldListener.get(nacosDynamicConfigService)).size());

            // 测试删除订阅
            Assert.assertTrue(dynamicConfigSubscribe.unSubscribe());
            Assert.assertEquals(0,
                    ((List<NacosListener>) fieldListener.get(nacosDynamicConfigService)).size());
            testListener.setChange(false);
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment",
                            "content:11"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment_service:testServiceName",
                            "content:22"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "testCustomLabel:testCustomLabelValue",
                            "content:33"));
            Thread.sleep(1000);
            Assert.assertFalse(testListener.isChange());
        } finally {
            nacosDynamicConfigService.doRemoveConfig("testSubscribeKey",
                    "app:testApplication_environment:testEnvironment");
            nacosDynamicConfigService.doRemoveConfig("testSubscribeKey",
                    "app:testApplication_environment:testEnvironment_service:testServiceName");
            nacosDynamicConfigService.doRemoveConfig("testSubscribeKey", "testCustomLabel:testCustomLabelValue");
        }
    }
}
