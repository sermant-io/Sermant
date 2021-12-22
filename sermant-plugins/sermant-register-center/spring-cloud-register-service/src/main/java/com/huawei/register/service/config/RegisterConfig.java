/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.service.config;

import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.plugin.config.PluginConfig;

/**
 * spring注册插件配置
 *
 * @author zhouss
 * @since 2021-12-16
 */
@ConfigTypeKey(value = "spring.cloud.register.plugin")
public class RegisterConfig implements PluginConfig {
    /**
     * sc注册中心地址
     * 多个地址使用逗号隔开
     */
    private String scUrls = "http://127.0.0.1:30100";

    /**
     * kie命名空间
     */
    private String scKieProject = "default";

    /**
     * 服务实例心跳发送间隔
     */
    private long heartbeatIntervalMs = 1000L;

    /**
     * sc app配置
     */
    private String app = "sermant";

    /**
     * sc 环境配置
     */
    private String environment = "production";

    /**
     * 默认注册框架类型
     */
    private String framework = "SpringCloud";

    /**
     * 框架版本
     */
    private String frameworkVersion = "N/A";

    /**
     * 默认sc版本
     */
    private String scVersion = "1.0.0";

    /**
     * 注册中心类型
     */
    private String registerType = "SERVICE_COMB";

    /**
     * 是否开启sc的加密
     */
    private boolean scSslEnabled = false;

    public boolean isScSslEnabled() {
        return scSslEnabled;
    }

    public void setScSslEnabled(boolean scSslEnabled) {
        this.scSslEnabled = scSslEnabled;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getScVersion() {
        return scVersion;
    }

    public void setScVersion(String scVersion) {
        this.scVersion = scVersion;
    }

    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getScKieProject() {
        return scKieProject;
    }

    public void setScKieProject(String scKieProject) {
        this.scKieProject = scKieProject;
    }

    public String getScUrls() {
        return scUrls;
    }

    public void setScUrls(String scUrls) {
        this.scUrls = scUrls;
    }

    public long getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(long heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
