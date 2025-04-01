/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.example.sermant.agentcore.test.application.tests.dynamicconfig;

import com.example.sermant.agentcore.test.application.results.DynamicConfigResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for dynamic configuration core functions
 *
 * @author tangle
 * @since 2023-09-08
 */
public class DynamicConfigTest {
    /**
     * Test configuration key, group, content, etc.
     */
    private static final String TEST_KEY_1 = "testKey1";

    private static final String TEST_KEY_2 = "testKey2";

    private static final String TEST_GROUP = "testGroup";

    private static final String TEST_CONTENT = "testContent";

    private static final String TEST_MODIFY_CONTENT = "testModifyContent";

    private static final long SLEEP_TIME_MILLIS = 1000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicConfigTest.class);

    /**
     * Result of the boolean array for testing plugin reflection result: listener success
     */
    private static boolean listenerSuccess;

    public static void setListenerSuccess(boolean flag) {
        listenerSuccess = flag;
    }

    /**
     * Test dynamic configuration functions
     *
     * @throws InterruptedException
     */
    public void testDynamicConfig() throws InterruptedException {
        testPublishConfig();
        testRemoveConfig();
        testAddConfigListener();
        testRemoveConfigListener();
        testAddGroupConfigListener();
        testRemoveGroupConfigListener();
    }

    /**
     * Test publish configuration
     *
     * @throws InterruptedException
     */
    public void testPublishConfig() throws InterruptedException {
        boolean result = false;

        // Publish configuration
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testPublishConfig' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Get configuration
        result = getConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the getConfig of 'testPublishConfig' method is: {}", result);
        DynamicConfigResults.DYNAMIC_PUBLISH_CONFIG.setResult(result);
    }

    /**
     * Test remove configuration
     *
     * @throws InterruptedException
     */
    public void testRemoveConfig() throws InterruptedException {
        boolean result = false;

        // Remove configuration
        result = removeConfig(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the removeConfig of 'testRemoveConfig' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Get configuration again, expected to be empty
        result = getConfig(false, TEST_KEY_1, TEST_GROUP, "");
        LOGGER.info("The result of the getConfig of 'testRemoveConfig' method is: {}", result);
        DynamicConfigResults.DYNAMIC_REMOVE_CONFIG.setResult(result);
    }

    /**
     * Test add single listener
     *
     * @throws InterruptedException
     */
    public void testAddConfigListener() throws InterruptedException {
        boolean result = false;

        // Add single configuration listener
        listenerSuccess = false;
        result = addConfigListener(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the addConfigListener of 'testAddConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Publish configuration
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testAddConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Check listener result, expected to be true
        DynamicConfigResults.DYNAMIC_ADD_CONFIG_LISTENER.setResult(listenerSuccess);
    }

    /**
     * Test remove single listener
     *
     * @throws InterruptedException
     */
    public void testRemoveConfigListener() throws InterruptedException {
        boolean result = false;

        // Remove single configuration listener
        result = removeConfigListener(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the removeConfigListener of 'testRemoveConfigListener' method is: {}",
                result);
        listenerSuccess = false;
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Modify configuration
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_MODIFY_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testRemoveConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Check listener result, expected to be false
        DynamicConfigResults.DYNAMIC_REMOVE_CONFIG_LISTENER.setResult(!listenerSuccess);
    }

    /**
     * Test add group listener
     *
     * @throws InterruptedException
     */
    public void testAddGroupConfigListener() throws InterruptedException {
        boolean result = false;

        // Publish configuration 1 and 2
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the first publishConfig of 'testAddGroupConfigListener' method is: {}",
                result);
        result = publishConfig(false, TEST_KEY_2, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the second publishConfig of 'testAddGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);
        listenerSuccess = false;

        // Add group configuration listener
        result = addGroupConfigListener(false, TEST_GROUP);
        LOGGER.info("The result of the addGroupConfigListener of 'testAddGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Modify configuration 1
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_MODIFY_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testAddGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Check listener result, expected to be true
        DynamicConfigResults.DYNAMIC_ADD_GROUP_CONFIG_LISTENER.setResult(listenerSuccess);
    }

    /**
     * Test remove group listener
     *
     * @throws InterruptedException
     */
    public void testRemoveGroupConfigListener() throws InterruptedException {
        boolean result = false;

        // Remove group configuration listener
        result = removeGroupConfigListener(false, TEST_GROUP);
        LOGGER.info("The result of the removeGroupConfigListener of 'testRemoveGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);
        listenerSuccess = false;

        // Remove configuration 1 and 2
        result = removeConfig(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the first removeConfig of 'testRemoveGroupConfigListener' method is: {}", result);
        result = removeConfig(false, TEST_KEY_2, TEST_GROUP);
        LOGGER.info("The result of the second removeConfig of 'testRemoveGroupConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // Check listener result, expected to be false
        DynamicConfigResults.DYNAMIC_REMOVE_GROUP_CONFIG_LISTENER.setResult(result & (!listenerSuccess));
    }

    /**
     * The following methods are test plugin intercept methods.
     * key: configuration name, group: configuration group, content: publish configuration content,
     * predictContent: get configuration prediction content, enhanceFlag: enhance result
     */
    private boolean publishConfig(boolean enhanceFlag, String key, String group, String content) {
        return enhanceFlag;
    }

    private boolean getConfig(boolean enhanceFlag, String key, String group, String predictContent) {
        return enhanceFlag;
    }

    private boolean removeConfig(boolean enhanceFlag, String key, String group) {
        return enhanceFlag;
    }

    private boolean addConfigListener(boolean enhanceFlag, String key, String group) {
        return enhanceFlag;
    }

    private boolean removeConfigListener(boolean enhanceFlag, String key, String group) {
        return enhanceFlag;
    }

    private boolean addGroupConfigListener(boolean enhanceFlag, String group) {
        return enhanceFlag;
    }

    private boolean removeGroupConfigListener(boolean enhanceFlag, String group) {
        return enhanceFlag;
    }
}
