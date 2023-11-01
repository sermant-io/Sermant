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

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public void testDynamicConfigSubscribe() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        try {
            // 测试订阅
            TestListener testListener = new TestListener();
            dynamicConfigSubscribe = new DynamicConfigSubscribe("testServiceName", testListener,
                    "testSubscribeKey");
            Assert.assertTrue(dynamicConfigSubscribe.subscribe());
            Thread.sleep(SLEEP_TIME_MILLIS);

            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment",
                            "content:1"));
            checkChangeTrue(testListener, "content:1");

            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment_service:testServiceName",
                            "content:2"));
            checkChangeTrue(testListener, "content:2");

            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "testCustomLabel:testCustomLabelValue",
                            "content:3"));
            checkChangeTrue(testListener, "content:3");

            Optional<Object> listeners = ReflectUtils.getFieldValueByClazz(nacosDynamicConfigService.getClass(),
                    nacosDynamicConfigService, "listeners");
            Assert.assertTrue(listeners.orElse(Collections.emptyList()) instanceof List);
            Assert.assertEquals(3,
                    ((List<NacosListener>) listeners.orElse(Collections.emptyList())).size());

            // 测试删除订阅
            Assert.assertTrue(dynamicConfigSubscribe.unSubscribe());
            Assert.assertEquals(0,
                    ((List<NacosListener>) listeners.orElse(Collections.emptyList())).size());
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment",
                            "content:11"));
            checkChangeFalse(testListener);

            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "app:testApplication_environment:testEnvironment_service:testServiceName",
                            "content:22"));
            checkChangeFalse(testListener);

            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSubscribeKey",
                            "testCustomLabel:testCustomLabelValue",
                            "content:33"));
            checkChangeFalse(testListener);
        } finally {
            nacosDynamicConfigService.doRemoveConfig("testSubscribeKey",
                    "app:testApplication_environment:testEnvironment");
            nacosDynamicConfigService.doRemoveConfig("testSubscribeKey",
                    "app:testApplication_environment:testEnvironment_service:testServiceName");
            nacosDynamicConfigService.doRemoveConfig("testSubscribeKey", "testCustomLabel:testCustomLabelValue");
        }
    }
}
