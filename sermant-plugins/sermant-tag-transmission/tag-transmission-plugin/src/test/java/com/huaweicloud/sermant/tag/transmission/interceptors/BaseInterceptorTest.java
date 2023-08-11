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

package com.huaweicloud.sermant.tag.transmission.interceptors;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * 流量标签透传UT基础测试类
 *
 * @author tangle
 * @since 2023-07-27
 */
public class BaseInterceptorTest {
    public final TagTransmissionConfig tagTransmissionConfig = new TagTransmissionConfig();

    public MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    public BaseInterceptorTest() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(TagTransmissionConfig.class))
                .thenReturn(tagTransmissionConfig);
    }

    @Before
    public void before() {
        tagTransmissionConfig.setEnabled(true);
        List<String> tagKeys = new ArrayList<>();
        tagKeys.add("id");
        tagKeys.add("name");
        tagTransmissionConfig.setTagKeys(tagKeys);
        TrafficUtils.removeTrafficTag();
    }

    @After
    public void after() {
        pluginConfigManagerMockedStatic.close();
    }
}
