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

package io.sermant.router.common.config;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.router.common.constants.RouterConstant;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Route configuration
 *
 * @author provenceee
 * @since 2021-11-18
 */
@ConfigTypeKey("router.plugin")
public class RouterConfig implements PluginConfig {
    /**
     * Route version
     */
    private String routerVersion = RouterConstant.ROUTER_DEFAULT_VERSION;

    /**
     * Region
     */
    private String zone;

    /**
     * After the compatibility switch is enabled, a route with the same label matching zone will be initialized in the
     * configuration cache of dubbo
     */
    @ConfigFieldKey("enabled-dubbo-zone-router")
    private boolean enabledDubboZoneRouter;

    /**
     * After the Spring Cloud region route is initialized, a zone matching route with the same label will be initialized
     * in the Spring Cloud configuration cache
     */
    @ConfigFieldKey("enabled-spring-zone-router")
    private boolean enabledSpringZoneRouter;

    /**
     * whether the registration plugin is suitable
     */
    @ConfigFieldKey("enabled-registry-plugin-adaptation")
    private boolean enabledRegistryPluginAdaptation;

    /**
     * whether to use the request information for routing
     */
    @ConfigFieldKey("use-request-router")
    private boolean useRequestRouter;

    /**
     * use the request information as a route tags
     */
    @ConfigFieldKey("request-tags")
    private List<String> requestTags;

    /**
     * other configurations
     */
    private Map<String, String> parameters;

    /**
     * the tag of the request header to be resolved
     */
    @ConfigFieldKey("parse-header-tag")
    private String parseHeaderTag;

    /**
     * compatibility router config 1.0, default is false not support
     */
    @ConfigFieldKey("enabled-previous-rule")
    private boolean enabledPreviousRule = false;

    /**
     * Constructor
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

    public boolean isEnabledRegistryPluginAdaptation() {
        return enabledRegistryPluginAdaptation;
    }

    public void setEnabledRegistryPluginAdaptation(boolean enabledRegistryPluginAdaptation) {
        this.enabledRegistryPluginAdaptation = enabledRegistryPluginAdaptation;
    }

    public boolean isUseRequestRouter() {
        return useRequestRouter;
    }

    public void setUseRequestRouter(boolean useRequestRouter) {
        this.useRequestRouter = useRequestRouter;
    }

    public List<String> getRequestTags() {
        return requestTags == null ? Collections.emptyList() : requestTags;
    }

    /**
     * The request header is changed to lowercase in the HTTP request
     *
     * @param requestTags use the request information as a route tags
     */
    public void setRequestTags(List<String> requestTags) {
        if (requestTags != null) {
            requestTags.replaceAll(tag -> tag.toLowerCase(Locale.ROOT));
            this.requestTags = requestTags;
        } else {
            this.requestTags = null;
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getParseHeaderTag() {
        return parseHeaderTag;
    }

    public void setParseHeaderTag(String parseHeaderTag) {
        this.parseHeaderTag = parseHeaderTag;
    }

    public boolean isEnabledPreviousRule() {
        return enabledPreviousRule;
    }

    public void setEnabledPreviousRule(boolean enabledPreviousRule) {
        this.enabledPreviousRule = enabledPreviousRule;
    }
}