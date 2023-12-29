/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.config;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;

/**
 * ProhibitionConfigManager单元测试
 *
 * @author lilai
 * @since 2023-12-23
 */
public class ProhibitionConfigManagerTest {
    private static ProhibitionConfig globalConfig;

    private static ProhibitionConfig localConfig;

    @BeforeClass
    public static void setUp() {
        globalConfig = new ProhibitionConfig();
        HashSet<String> globalKafkaTopics = new HashSet<>();
        globalKafkaTopics.add("testKafkaTopic-1");
        HashSet<String> globalRocketMqTopics = new HashSet<>();
        globalRocketMqTopics.add("testRocketMqTopic-1");
        globalConfig.setKafkaTopics(globalKafkaTopics);
        globalConfig.setRocketMqTopics(globalRocketMqTopics);
        localConfig = new ProhibitionConfig();
        HashSet<String> localKafkaTopics = new HashSet<>();
        localKafkaTopics.add("testKafkaTopic-2");
        HashSet<String> localRocketMqTopics = new HashSet<>();
        localRocketMqTopics.add("testRocketMqTopic-2");
        localConfig.setKafkaTopics(localKafkaTopics);
        localConfig.setRocketMqTopics(localRocketMqTopics);
    }

    /**
     * 测试Global和Local配置都开启的情况
     */
    @Test
    public void testGetKafkaProhibitionTopicsWithGlobalAndLocalConfigEnabled() {
        globalConfig.setEnableKafkaProhibition(true);
        localConfig.setEnableKafkaProhibition(true);
        globalConfig.setEnableRocketMqProhibition(true);
        localConfig.setEnableRocketMqProhibition(true);
        ProhibitionConfigManager.updateGlobalConfig(globalConfig);
        ProhibitionConfigManager.updateLocalConfig(localConfig);

        Assert.assertEquals(globalConfig.getKafkaTopics(), ProhibitionConfigManager.getKafkaProhibitionTopics());
        Assert.assertEquals(globalConfig.getRocketMqTopics(), ProhibitionConfigManager.getRocketMqProhibitionTopics());
    }

    /**
     * 测试Global配置开启的情况
     */
    @Test
    public void testGetKafkaProhibitionTopicsWithJustGlobalConfigEnabled() {
        globalConfig.setEnableKafkaProhibition(true);
        localConfig.setEnableKafkaProhibition(false);
        globalConfig.setEnableRocketMqProhibition(true);
        localConfig.setEnableRocketMqProhibition(false);
        ProhibitionConfigManager.updateGlobalConfig(globalConfig);
        ProhibitionConfigManager.updateLocalConfig(localConfig);

        Assert.assertEquals(globalConfig.getKafkaTopics(), ProhibitionConfigManager.getKafkaProhibitionTopics());
        Assert.assertEquals(globalConfig.getRocketMqTopics(), ProhibitionConfigManager.getRocketMqProhibitionTopics());
    }

    /**
     * 测试Local配置开启的情况
     */
    @Test
    public void testGetKafkaProhibitionTopicsWithJustLocalConfigEnabled() {
        globalConfig.setEnableKafkaProhibition(false);
        localConfig.setEnableKafkaProhibition(true);
        globalConfig.setEnableRocketMqProhibition(false);
        localConfig.setEnableRocketMqProhibition(true);
        ProhibitionConfigManager.updateGlobalConfig(globalConfig);
        ProhibitionConfigManager.updateLocalConfig(localConfig);

        Assert.assertEquals(localConfig.getKafkaTopics(), ProhibitionConfigManager.getKafkaProhibitionTopics());
        Assert.assertEquals(localConfig.getRocketMqTopics(), ProhibitionConfigManager.getRocketMqProhibitionTopics());
    }

    /**
     * 测试Global和Local配置都关闭的情况
     */
    @Test
    public void testGetKafkaProhibitionTopicsWithBothConfigsDisabled() {
        globalConfig.setEnableKafkaProhibition(false);
        localConfig.setEnableKafkaProhibition(false);
        globalConfig.setEnableRocketMqProhibition(false);
        localConfig.setEnableRocketMqProhibition(false);
        ProhibitionConfigManager.updateGlobalConfig(globalConfig);
        ProhibitionConfigManager.updateLocalConfig(localConfig);

        Assert.assertTrue(ProhibitionConfigManager.getKafkaProhibitionTopics().isEmpty());
        Assert.assertTrue(ProhibitionConfigManager.getRocketMqProhibitionTopics().isEmpty());
    }

    /**
     * 测试更新配置不为null的情况
     */
    @Test
    public void testUpdateConfigWithNonNullConfig() {
        ProhibitionConfigManager.updateGlobalConfig(globalConfig);
        ProhibitionConfigManager.updateLocalConfig(localConfig);
        Assert.assertEquals(globalConfig, ProhibitionConfigManager.getGlobalConfig());
    }

    /**
     * 测试更新配置为null的情况
     */
    @Test
    public void testUpdateConfigWithNullConfig() {
        ProhibitionConfigManager.updateGlobalConfig(null);
        ProhibitionConfigManager.updateLocalConfig(null);

        Assert.assertEquals(0, ProhibitionConfigManager.getGlobalConfig().getKafkaTopics().size());
        Assert.assertEquals(0, ProhibitionConfigManager.getGlobalConfig().getRocketMqTopics().size());
        Assert.assertFalse(ProhibitionConfigManager.getGlobalConfig().isEnableKafkaProhibition());
        Assert.assertFalse(ProhibitionConfigManager.getGlobalConfig().isEnableRocketMqProhibition());
        Assert.assertEquals(0, ProhibitionConfigManager.getLocalConfig().getKafkaTopics().size());
        Assert.assertEquals(0, ProhibitionConfigManager.getLocalConfig().getRocketMqTopics().size());
        Assert.assertFalse(ProhibitionConfigManager.getLocalConfig().isEnableKafkaProhibition());
        Assert.assertFalse(ProhibitionConfigManager.getLocalConfig().isEnableRocketMqProhibition());
    }
}
