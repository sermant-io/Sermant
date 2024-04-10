/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring registration plugin configuration
 *
 * @author zhouss
 * @since 2021-12-16
 */
@ConfigTypeKey(value = "servicecomb.service")
public class RegisterConfig implements PluginConfig {
    /**
     * kie namespace
     */
    private String project = ConfigConstants.COMMON_DEFAULT_VALUE;

    /**
     * The heartbeat sending interval of the service instance
     */
    private int heartbeatInterval = ConfigConstants.DEFAULT_HEARTBEAT_INTERVAL;

    /**
     * The number of heartbeat retries
     */
    private int heartbeatRetryTimes = ConfigConstants.DEFAULT_HEARTBEAT_RETRY_TIMES;

    /**
     * The interval between the pull instances
     */
    private int pullInterval = ConfigConstants.DEFAULT_PULL_INTERVAL;

    /**
     * sc app configuration
     */
    private String application = "sermant";

    /**
     * sc Environment configuration
     */
    private String environment = "";

    /**
     * Default SC version
     */
    private String version = "1.0.0";

    /**
     * Whether to enable SC encryption As a configuration class, you cannot use the Boolean type that starts with is,
     * otherwise the configuration cannot be read
     */
    private boolean sslEnabled = false;

    /**
     * Specifies whether to enable the migration mode
     */
    private boolean openMigration = false;

    /**
     * Spring registration switch
     */
    private boolean enableSpringRegister = false;

    /**
     * dubbo registration switch
     */
    private boolean enableDubboRegister = false;

    /**
     * Whether to enable region discovery
     */
    private boolean enableZoneAware = false;

    /**
     * The name of the data center
     */
    private String dataCenterName = ConfigConstants.COMMON_DEFAULT_VALUE;

    /**
     * Data Center region
     */
    private String dataCenterRegion = ConfigConstants.COMMON_DEFAULT_VALUE;

    /**
     * Data Center Go
     */
    private String dataCenterAvailableZone = ConfigConstants.COMMON_DEFAULT_VALUE;

    /**
     * Specifies whether to access instances across apps
     */
    private boolean allowCrossApp = false;

    /**
     * If the Spring Cloud Zone does not use the default system environment variables, that is,
     * spring.cloud.loadbalancer.zone
     */
    private String zone;

    /**
     * If you want to use the IP address to access the hostname, you can choose whether to use the IP address to access
     * the downstream data
     */
    private boolean preferIpAddress = false;

    /**
     * Whether to ignore the contract differences
     */
    private boolean ignoreSwaggerDifferent = false;

    /**
     * Whitelist of dubbo parameters
     */
    private List<String> governanceParametersWhiteList = Collections.singletonList("timeout");

    /**
     * The interface-level parameter key during dubbo registration
     */
    private List<String> interfaceKeys;

    /**
     * Service level parameters, in the form of:k1,v1;k2,v2
     */
    private String parameters;

    /**
     * Constructor
     */
    public RegisterConfig() {
        final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        if (serviceMeta == null) {
            return;
        }
        this.environment = serviceMeta.getEnvironment();
        this.application = serviceMeta.getApplication();
        this.project = serviceMeta.getProject();
        this.version = serviceMeta.getVersion();
    }

    public boolean isPreferIpAddress() {
        return preferIpAddress;
    }

    public void setPreferIpAddress(boolean preferIpAddress) {
        this.preferIpAddress = preferIpAddress;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isAllowCrossApp() {
        return allowCrossApp;
    }

    public void setAllowCrossApp(boolean allowCrossApp) {
        this.allowCrossApp = allowCrossApp;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public String getDataCenterRegion() {
        return dataCenterRegion;
    }

    public void setDataCenterRegion(String dataCenterRegion) {
        this.dataCenterRegion = dataCenterRegion;
    }

    public String getDataCenterAvailableZone() {
        return dataCenterAvailableZone;
    }

    public void setDataCenterAvailableZone(String dataCenterAvailableZone) {
        this.dataCenterAvailableZone = dataCenterAvailableZone;
    }

    public boolean isEnableZoneAware() {
        return enableZoneAware;
    }

    public void setEnableZoneAware(boolean enableZoneAware) {
        this.enableZoneAware = enableZoneAware;
    }

    public boolean isEnableSpringRegister() {
        return enableSpringRegister;
    }

    public void setEnableSpringRegister(boolean enableSpringRegister) {
        this.enableSpringRegister = enableSpringRegister;
    }

    public boolean isEnableDubboRegister() {
        return enableDubboRegister;
    }

    public void setEnableDubboRegister(boolean enableDubboRegister) {
        this.enableDubboRegister = enableDubboRegister;
    }

    public boolean isOpenMigration() {
        return openMigration;
    }

    public void setOpenMigration(boolean openMigration) {
        this.openMigration = openMigration;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHeartbeatRetryTimes() {
        return heartbeatRetryTimes;
    }

    public void setHeartbeatRetryTimes(int heartbeatRetryTimes) {
        this.heartbeatRetryTimes = heartbeatRetryTimes;
    }

    public int getPullInterval() {
        return pullInterval;
    }

    public void setPullInterval(int pullInterval) {
        this.pullInterval = pullInterval;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isIgnoreSwaggerDifferent() {
        return ignoreSwaggerDifferent;
    }

    public void setIgnoreSwaggerDifferent(boolean ignoreSwaggerDifferent) {
        this.ignoreSwaggerDifferent = ignoreSwaggerDifferent;
    }

    public List<String> getGovernanceParametersWhiteList() {
        return governanceParametersWhiteList;
    }

    public void setGovernanceParametersWhiteList(List<String> governanceParametersWhiteList) {
        this.governanceParametersWhiteList = governanceParametersWhiteList;
    }

    public List<String> getInterfaceKeys() {
        return interfaceKeys;
    }

    public void setInterfaceKeys(List<String> interfaceKeys) {
        this.interfaceKeys = interfaceKeys;
    }

    /**
     * Obtain the registration parameters
     *
     * @return Registration parameters
     */
    public Map<String, String> getParametersMap() {
        if (StringUtils.isBlank(parameters)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        String[] kvs = parameters.trim().split(",");
        for (String kv : kvs) {
            String[] arr = kv.trim().split(":");
            if (arr.length > 0) {
                map.put(arr[0].trim(), arr.length > 1 ? arr[1].trim() : "");
            }
        }
        return map;
    }

    public String getParameters() {
        return this.parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
