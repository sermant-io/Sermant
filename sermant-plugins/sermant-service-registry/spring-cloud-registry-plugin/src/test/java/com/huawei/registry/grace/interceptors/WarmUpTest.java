/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.netflix.loadbalancer.Server;

import org.junit.Assert;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预热
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class WarmUpTest {
    /**
     * 默认端口
     */
    protected static final int DEFAULT_PORT = 8989;

    /**
     * 默认服务名
     */
    protected static final String DEFAULT_SERVICE_NAME = "test";

    /**
     * 禁用预热的IP
     */
    protected static final String DISABLE_WARM_UP_IP = "127.0.0.1";

    /**
     * 开启预热的IP
     */
    protected static final String ENABLE_WARM_UP_IP = "127.0.0.2";

    /**
     * 最大请求数
     */
    protected static final int REQUEST_COUNT = 1000;

    /**
     * 默认预热时间
     */
    protected static final int DEFAULT_WARM_TIME = 120;

    /**
     * 请求相差倍率
     */
    protected static final double RATE = 3d;

    /**
     * 配置
     */
    protected final GraceConfig graceConfig = new GraceConfig();

    /**
     * 前置
     */
    protected void before() {
        graceConfig.setEnableWarmUp(true);
        graceConfig.setEnableSpring(true);
        graceConfig.setWarmUpTime(DEFAULT_WARM_TIME);
        final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
    }

    /**
     * 统计数据
     *
     * @param result 结果
     * @param statResult 统计结果
     */
    protected void stat(Object result, HashMap<String, Integer> statResult) {
        List servers = (List) result;
        if (servers == null || servers.isEmpty()) {
            return;
        }
        final Object server = servers.get(0);
        String host = null;
        if (server instanceof ServiceInstance) {
            host = ((ServiceInstance) server).getHost();
        } else if (server instanceof Server) {
            host = ((Server) server).getHost();
        } else {
            Assert.fail();
        }
        final Integer count = statResult.getOrDefault(host, 0);
        statResult.put(host, count + 1);
    }

    /**
     * 构建实例
     *
     * @param ip ip地址
     * @return 实例
     */
    protected MicroServiceInstance microServiceInstance(String ip) {
        return new MicroServiceInstance() {
            private final Map<String, String> meta = new HashMap<>();

            @Override
            public String getServiceName() {
                return DEFAULT_SERVICE_NAME;
            }

            @Override
            public String getHost() {
                return ip;
            }

            @Override
            public String getIp() {
                return ip;
            }

            @Override
            public int getPort() {
                return DEFAULT_PORT;
            }

            @Override
            public String getServiceId() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public Map<String, String> getMetadata() {
                return meta;
            }
        };
    }
}
