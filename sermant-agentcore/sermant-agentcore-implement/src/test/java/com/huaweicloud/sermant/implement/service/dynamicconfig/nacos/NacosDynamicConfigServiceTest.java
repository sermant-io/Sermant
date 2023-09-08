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
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Nacos动态配置服务功能测试
 *
 * @author tangle
 * @since 2023-09-08
 */
public class NacosDynamicConfigServiceTest extends NacosBaseTest {
    @Test
    public void test() throws Exception {
        try {
            // 测试发布和获取配置：合法group名称
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testTrueSingleConfigKey",
                            "test.True&Single:Config-Group",
                            "testTrueSingleConfigContent"));
            Thread.sleep(1000);
            Assert.assertEquals("testTrueSingleConfigContent",
                    nacosDynamicConfigService.doGetConfig("testTrueSingleConfigKey",
                            "test.True_Single:Config-Group").orElse(""));

            // 测试发布和获取配置：不合法group名称
            Assert.assertFalse(
                    nacosDynamicConfigService.doPublishConfig("testErrorSingleConfigKey", "test+++Error&Single"
                            + ":Config-Group", "testErrorSingleConfigContent"));
            Assert.assertEquals(Optional.empty(), nacosDynamicConfigService.doGetConfig("testErrorSingleConfigKey",
                    "test+++.Error_Single:Config-Group"));

            // 测试移除配置
            Assert.assertTrue(nacosDynamicConfigService.doRemoveConfig("testTrueSingleConfigKey", "test.True_Single"
                    + ":Config-Group"));
            Assert.assertEquals("",
                    nacosDynamicConfigService.doGetConfig("testTrueSingleConfigKey",
                            "test.True_Single:Config-Group").orElse(""));

            // 测试监听器的添加
            TestListener testListener = new TestListener();
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doAddConfigListener("testSingleListenerKey", "testSingleListenerGroup",
                            testListener));
            Thread.sleep(1000);
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent-3"));
            Thread.sleep(1000);
            Assert.assertTrue(testListener.isChange());
            testListener.setChange(false);

            // 测试监听器的移除
            Assert.assertTrue(nacosDynamicConfigService.doRemoveConfigListener("testSingleListenerKey",
                    "testSingleListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent-3"));
            Thread.sleep(1000);
            Assert.assertFalse(testListener.isChange());
            Assert.assertTrue(
                    nacosDynamicConfigService.doRemoveConfig("testSingleListenerKey", "testSingleListenerGroup"));

            // 测试组监听器的添加、获取组内所有key
            TestListener testListenerGroup = new TestListener();
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-1", "testGroupListenerGroup",
                            "testGroupListenerContent-1"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-2", "testGroupListenerGroup",
                            "testGroupListenerContent-2"));
            Assert.assertTrue(nacosDynamicConfigService.doAddGroupListener("testGroupListenerGroup",
                    testListenerGroup));
            Thread.sleep(1000);
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-2", "testGroupListenerGroup",
                            "testGroupListenerContent-2-2"));
            Assert.assertEquals(2, nacosDynamicConfigService.doListKeysFromGroup("testGroupListenerGroup").size());
            Thread.sleep(1000);
            Assert.assertTrue(testListenerGroup.isChange());

            // 测试组监听器定时任务自动更新
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-3", "testGroupListenerGroup",
                            "testGroupListenerContent-3"));
            Thread.sleep(1000);
            Whitebox.invokeMethod(nacosDynamicConfigService, "updateConfigListener");
            Thread.sleep(1000);
            Field fieldListener = nacosDynamicConfigService.getClass().getDeclaredField("listeners");
            fieldListener.setAccessible(true);
            Assert.assertEquals(3,
                    ((List<NacosListener>) fieldListener.get(nacosDynamicConfigService)).get(0).getKeyListener()
                            .size());
            testListener.setChange(false);

            // 测试组监听器的移除
            Assert.assertTrue(nacosDynamicConfigService.doRemoveGroupListener("testGroupListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-4", "testGroupListenerGroup",
                            "testGroupListenerKey-4"));
            Thread.sleep(1000);
            Assert.assertFalse(testListener.isChange());
            Assert.assertTrue(
                    nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-1", "testGroupListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-2", "testGroupListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-3", "testGroupListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-4", "testGroupListenerGroup"));
        } finally {
            nacosDynamicConfigService.doRemoveConfig("testTrueSingleConfigKey", "test.True_Single"
                    + ":Config-Group");
            nacosDynamicConfigService.doRemoveConfigListener("testSingleListenerKey",
                    "testSingleListenerGroup");
            nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-1", "testGroupListenerGroup");
            nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-2", "testGroupListenerGroup");
            nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-3", "testGroupListenerGroup");
            nacosDynamicConfigService.doRemoveConfig("testGroupListenerKey-4", "testGroupListenerGroup");
        }
    }
}
