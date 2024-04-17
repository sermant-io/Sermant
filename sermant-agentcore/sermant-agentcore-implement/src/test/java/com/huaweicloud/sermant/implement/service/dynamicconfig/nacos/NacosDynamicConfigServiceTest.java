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
import org.powermock.reflect.Whitebox;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Nacos dynamic configuration service function testing
 *
 * @author tangle
 * @since 2023-09-08
 */
public class NacosDynamicConfigServiceTest extends NacosBaseTest {
    @Test
    public void testNacosDynamicConfigService() throws Exception {
        try {
            // Test publish and get configuration: valid group name
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testTrueSingleConfigKey",
                            "test.True&Single:Config-Group",
                            "testTrueSingleConfigContent"));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertEquals("testTrueSingleConfigContent",
                    nacosDynamicConfigService.doGetConfig("testTrueSingleConfigKey",
                            "test.True_Single:Config-Group").orElse(""));

            // Test publish and get configuration: invalid group name
            Assert.assertFalse(
                    nacosDynamicConfigService.doPublishConfig("testErrorSingleConfigKey", "test+++Error&Single"
                            + ":Config-Group", "testErrorSingleConfigContent"));
            Assert.assertEquals(Optional.empty(), nacosDynamicConfigService.doGetConfig("testErrorSingleConfigKey",
                    "test+++.Error_Single:Config-Group"));

            // Test remove configuration
            Assert.assertTrue(nacosDynamicConfigService.doRemoveConfig("testTrueSingleConfigKey", "test.True_Single"
                    + ":Config-Group"));
            Assert.assertEquals("",
                    nacosDynamicConfigService.doGetConfig("testTrueSingleConfigKey",
                            "test.True_Single:Config-Group").orElse(""));

            // Test listener addition
            TestListener testListener = new TestListener();
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doAddConfigListener("testSingleListenerKey", "testSingleListenerGroup",
                            testListener));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent-3"));
            checkChangeTrue(testListener, "testSingleListenerContent-3");

            // Test listener removal
            Assert.assertTrue(nacosDynamicConfigService.doRemoveConfigListener("testSingleListenerKey",
                    "testSingleListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testSingleListenerKey", "testSingleListenerGroup",
                            "testSingleListenerContent-3"));
            checkChangeFalse(testListener);
            Assert.assertTrue(
                    nacosDynamicConfigService.doRemoveConfig("testSingleListenerKey", "testSingleListenerGroup"));

            // Test the addition of group listeners and get all keys in the group
            TestListener testListenerGroup = new TestListener();
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-1", "testGroupListenerGroup",
                            "testGroupListenerContent-1"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-2", "testGroupListenerGroup",
                            "testGroupListenerContent-2"));
            Assert.assertTrue(nacosDynamicConfigService.doAddGroupListener("testGroupListenerGroup",
                    testListenerGroup));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-2", "testGroupListenerGroup",
                            "testGroupListenerContent-2-2"));
            Assert.assertEquals(2, nacosDynamicConfigService.doListKeysFromGroup("testGroupListenerGroup").size());
            checkChangeTrue(testListenerGroup, "testGroupListenerContent-2-2");

            // test the group listener automatically updates scheduled tasks
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-3", "testGroupListenerGroup",
                            "testGroupListenerContent-3"));
            Thread.sleep(SLEEP_TIME_MILLIS);
            Whitebox.invokeMethod(nacosDynamicConfigService, "updateConfigListener");
            Thread.sleep(SLEEP_TIME_MILLIS);

            Optional<Object> listeners = ReflectUtils.getFieldValueByClazz(nacosDynamicConfigService.getClass(),
                    nacosDynamicConfigService, "listeners");
            Assert.assertTrue(listeners.orElse(Collections.emptyList()) instanceof List);
            Assert.assertEquals(3,
                    ((List<NacosListener>) listeners.orElse(Collections.emptyList())).get(0).getKeyListener()
                            .size());

            // Test removal of group listeners
            testListenerGroup.setChange(false);
            Assert.assertTrue(nacosDynamicConfigService.doRemoveGroupListener("testGroupListenerGroup"));
            Assert.assertTrue(
                    nacosDynamicConfigService.doPublishConfig("testGroupListenerKey-4", "testGroupListenerGroup",
                            "testGroupListenerKey-4"));
            checkChangeFalse(testListenerGroup);
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
