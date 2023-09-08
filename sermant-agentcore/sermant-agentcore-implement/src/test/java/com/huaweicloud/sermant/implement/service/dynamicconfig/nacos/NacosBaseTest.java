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

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.service.dynamicconfig.config.DynamicConfig;
import com.huaweicloud.sermant.core.utils.AesUtil;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Nacos动态配置基础测试
 *
 * @author tangle
 * @since 2023-09-08
 */
public class NacosBaseTest {
    /**
     * Nacos动态配置服务
     */
    public NacosDynamicConfigService nacosDynamicConfigService;

    /**
     * 配置类
     */
    public final DynamicConfig dynamicConfig = new DynamicConfig();

    public final ServiceMeta serviceMeta = new ServiceMeta();

    public MockedStatic<ConfigManager> dynamicConfigMockedStatic;

    public MockedStatic<ServiceManager> serviceManagerMockedStatic;

    /**
     * 测试监听器子类
     */
    public class TestListener implements DynamicConfigListener {
        private boolean isChange = false;

        @Override
        public void process(DynamicConfigEvent event) {
            setChange(true);
        }

        public boolean isChange() {
            return isChange;
        }

        public void setChange(boolean change) {
            isChange = change;
        }
    }

    /**
     * 初始配置设置
     */
    @Before
    public void initConfig() {
        dynamicConfig.setEnableAuth(true);
        dynamicConfig.setServerAddress("127.0.0.1:8848");
        dynamicConfig.setTimeoutValue(30000);
        Optional<String> optional = AesUtil.generateKey();
        dynamicConfig.setPrivateKey(optional.orElse(""));
        dynamicConfig.setUserName(AesUtil.encrypt(optional.get(), "nacos").orElse(""));
        dynamicConfig.setPassword(AesUtil.encrypt(optional.get(), "nacos").orElse(""));
        serviceMeta.setProject("testProject2");
        serviceMeta.setApplication("testApplication");
        serviceMeta.setEnvironment("testEnvironment");
        serviceMeta.setCustomLabel("testCustomLabel");
        serviceMeta.setCustomLabelValue("testCustomLabelValue");
        dynamicConfigMockedStatic = Mockito.mockStatic(ConfigManager.class);
        dynamicConfigMockedStatic.when(() -> ConfigManager.getConfig(DynamicConfig.class))
                .thenReturn(dynamicConfig);
        dynamicConfigMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class))
                .thenReturn(serviceMeta);

        nacosDynamicConfigService = new NacosDynamicConfigService();
        nacosDynamicConfigService.start();

        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(NacosDynamicConfigService.class))
                .thenReturn(nacosDynamicConfigService);
    }

    @After
    public void closeMock() {
        dynamicConfigMockedStatic.close();
        serviceManagerMockedStatic.close();
    }
}
