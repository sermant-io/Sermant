/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.config;

import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;

import java.util.Map;

/**
 * Configure the information of current service instance
 *
 * @author zhouss
 * @since 2022-02-09
 */
@ConfigTypeKey("service.meta")
public class ServiceMeta implements BaseConfig {
    /**
     * default value
     */
    public static final String DEFAULT = "default";

    /**
     * Service name
     */
    private String service = DEFAULT;

    /**
     * Application name
     */
    private String application = DEFAULT;

    /**
     * Current version
     */
    private String version = "0.0.0";

    /**
     * Zone
     */
    private String zone = DEFAULT;

    /**
     * Project
     */
    private String project = DEFAULT;

    /**
     * Environment
     */
    private String environment = "";

    /**
     * Custom label
     */
    private String customLabel = "public";

    /**
     * Custom label value
     */
    private String customLabelValue = DEFAULT;

    private Map<String, String> parameters;

    public String getCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }

    public String getCustomLabelValue() {
        return customLabelValue;
    }

    public void setCustomLabelValue(String customLabelValue) {
        this.customLabelValue = customLabelValue;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEnvironment() {
        return environment == null ? "" : environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
