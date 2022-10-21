/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.common.config;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.config.common.ConfigFieldKey;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;

import java.util.Map;

/**
 * 路由配置
 *
 * @author provenceee
 * @since 2021-11-18
 */
@ConfigTypeKey("router.plugin")
public class RouterConfig implements PluginConfig {
    /**
     * 路由版本
     */
    private String routerVersion = RouterConstant.ROUTER_DEFAULT_VERSION;

    /**
     * 区域
     */
    private String zone;

    /**
     * 是否开启dubbo区域路由
     */
    @ConfigFieldKey("enabled-dubbo-zone-router")
    private boolean enabledDubboZoneRouter;

    /**
     * 是否开启spring cloud区域路由
     */
    @ConfigFieldKey("enabled-spring-zone-router")
    private boolean enabledSpringZoneRouter;

    /**
     * 是否开启服务注册发现插件区域路由
     */
    @ConfigFieldKey("enabled-registry-zone-router")
    private boolean enabledRegistryZoneRouter;

    /**
     * 其它配置
     */
    private Map<String, String> parameters;

    /**
     * 构造方法
     */
    public RouterConfig() {
        ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        if (serviceMeta == null) {
            return;
        }
        this.routerVersion = serviceMeta.getVersion();
        this.zone = serviceMeta.getZone();
        this.parameters = serviceMeta.getParameters();
    }

    public String getRouterVersion() {
        return routerVersion;
    }

    public void setRouterVersion(String routerVersion) {
        this.routerVersion = routerVersion;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isEnabledDubboZoneRouter() {
        return enabledDubboZoneRouter;
    }

    public void setEnabledDubboZoneRouter(boolean enabledDubboZoneRouter) {
        this.enabledDubboZoneRouter = enabledDubboZoneRouter;
    }

    public boolean isEnabledSpringZoneRouter() {
        return enabledSpringZoneRouter;
    }

    public void setEnabledSpringZoneRouter(boolean enabledSpringZoneRouter) {
        this.enabledSpringZoneRouter = enabledSpringZoneRouter;
    }

    public boolean isEnabledRegistryZoneRouter() {
        return enabledRegistryZoneRouter;
    }

    public void setEnabledRegistryZoneRouter(boolean enabledRegistryZoneRouter) {
        this.enabledRegistryZoneRouter = enabledRegistryZoneRouter;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}