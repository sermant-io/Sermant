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
 * 测试动态配置核心功能
 *
 * @author tangle
 * @since 2023-09-08
 */
public class DynamicConfigTest {
    /**
     * 测试所用的配置名、配置组、配置内容等
     */
    private static final String TEST_KEY_1 = "testKey1";

    private static final String TEST_KEY_2 = "testKey2";

    private static final String TEST_GROUP = "testGroup";

    private static final String TEST_CONTENT = "testContent";

    private static final String TEST_MODIFY_CONTENT = "testModifyContent";

    private static final long SLEEP_TIME_MILLIS = 1000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicConfigTest.class);

    /**
     * 用于测试插件反射修改的回执结果：监听成功
     */
    private static boolean listenerSuccess;

    public static void setListenerSuccess(boolean flag) {
        listenerSuccess = flag;
    }

    /**
     * 测试动态配置功能
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
     * 测试发布配置
     *
     * @throws InterruptedException
     */
    public void testPublishConfig() throws InterruptedException {
        boolean result = false;

        // 发布配置
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testPublishConfig' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 获取配置
        result = getConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the getConfig of 'testPublishConfig' method is: {}", result);
        DynamicConfigResults.DYNAMIC_PUBLISH_CONFIG.setResult(result);
    }

    /**
     * 测试移除配置
     *
     * @throws InterruptedException
     */
    public void testRemoveConfig() throws InterruptedException {
        boolean result = false;

        // 删除配置
        result = removeConfig(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the removeConfig of 'testRemoveConfig' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 再次获取配置，预计获取为空
        result = getConfig(false, TEST_KEY_1, TEST_GROUP, "");
        LOGGER.info("The result of the getConfig of 'testRemoveConfig' method is: {}", result);
        DynamicConfigResults.DYNAMIC_REMOVE_CONFIG.setResult(result);
    }

    /**
     * 测试添加单一监听
     *
     * @throws InterruptedException
     */
    public void testAddConfigListener() throws InterruptedException {
        boolean result = false;

        // 添加单一配置监听
        listenerSuccess = false;
        result = addConfigListener(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the addConfigListener of 'testAddConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 发布配置
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testAddConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 查看监听结果，预计为true
        DynamicConfigResults.DYNAMIC_ADD_CONFIG_LISTENER.setResult(listenerSuccess);
    }

    /**
     * 测试移除单一监听
     *
     * @throws InterruptedException
     */
    public void testRemoveConfigListener() throws InterruptedException {
        boolean result = false;

        // 删除单一配置监听
        result = removeConfigListener(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the removeConfigListener of 'testRemoveConfigListener' method is: {}",
                result);
        listenerSuccess = false;
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 修改配置
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_MODIFY_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testRemoveConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 查看监听结果，预计为false
        DynamicConfigResults.DYNAMIC_REMOVE_CONFIG_LISTENER.setResult(!listenerSuccess);
    }

    /**
     * 测试添加组监听
     *
     * @throws InterruptedException
     */
    public void testAddGroupConfigListener() throws InterruptedException {
        boolean result = false;

        // 发布配置1和2
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the first publishConfig of 'testAddGroupConfigListener' method is: {}",
                result);
        result = publishConfig(false, TEST_KEY_2, TEST_GROUP, TEST_CONTENT);
        LOGGER.info("The result of the second publishConfig of 'testAddGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);
        listenerSuccess = false;

        // 添加组配置监听
        result = addGroupConfigListener(false, TEST_GROUP);
        LOGGER.info("The result of the addGroupConfigListener of 'testAddGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 修改配置1
        result = publishConfig(false, TEST_KEY_1, TEST_GROUP, TEST_MODIFY_CONTENT);
        LOGGER.info("The result of the publishConfig of 'testAddGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 查看监听结果，预计为true
        DynamicConfigResults.DYNAMIC_ADD_GROUP_CONFIG_LISTENER.setResult(listenerSuccess);
    }

    /**
     * 测试移除组监听
     *
     * @throws InterruptedException
     */
    public void testRemoveGroupConfigListener() throws InterruptedException {
        boolean result = false;

        // 删除单一配置监听
        result = removeGroupConfigListener(false, TEST_GROUP);
        LOGGER.info("The result of the removeGroupConfigListener of 'testRemoveGroupConfigListener' method is: {}",
                result);
        Thread.sleep(SLEEP_TIME_MILLIS);
        listenerSuccess = false;

        // 删除配置1和2
        result = removeConfig(false, TEST_KEY_1, TEST_GROUP);
        LOGGER.info("The result of the first removeConfig of 'testRemoveGroupConfigListener' method is: {}", result);
        result = removeConfig(false, TEST_KEY_2, TEST_GROUP);
        LOGGER.info("The result of the second removeConfig of 'testRemoveGroupConfigListener' method is: {}", result);
        Thread.sleep(SLEEP_TIME_MILLIS);

        // 查看监听结果，预计为false
        DynamicConfigResults.DYNAMIC_REMOVE_GROUP_CONFIG_LISTENER.setResult(result & (!listenerSuccess));
    }

    /**
     * 以下方法均为测试插件拦截方法，key：配置名、group：配置组、content：发布配置内容、predictContent：获取配置预测内容、enhanceFlag：增强回执
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
