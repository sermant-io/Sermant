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

package com.huawei.register.config;

import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.plugin.config.PluginConfig;
import com.huawei.sermant.core.service.meta.ServiceMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * spring注册插件配置
 *
 * @author zhouss
 * @since 2021-12-16
 */
@ConfigTypeKey(value = "servicecomb.service")
public class RegisterConfig implements PluginConfig {
    /**
     * sc注册中心地址 多个地址使用逗号隔开
     */
    private String address = "http://127.0.0.1:30100";

    /**
     * kie命名空间
     */
    private String project = "default";

    /**
     * 服务实例心跳发送间隔
     */
    private int heartbeatInterval = ConfigConstants.DEFAULT_HEARTBEAT_INTERVAL;

    /**
     * 心跳重试次数
     */
    private int heartbeatRetryTimes = ConfigConstants.DEFAULT_HEARTBEAT_RETRY_TIMES;

    /**
     * 拉取实例时间间隔
     */
    private int pullInterval = ConfigConstants.DEFAULT_PULL_INTERVAL;

    /**
     * sc app配置
     */
    private String application = "sermant";

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
    private String version = "1.0.0";

    /**
     * 注册中心类型
     */
    private String registerType = "SERVICE_COMB";

    /**
     * 是否开启sc的加密 作为配置类，使用布尔类型不可使用is开头，否则存在配置无法读取的问题
     */
    @SuppressWarnings("checkstyle:RegexpSingleLine")
    private boolean sslEnabled = false;

    /**
     * 是否开启迁移模式
     */
    @SuppressWarnings("checkstyle:RegexpSingleLine")
    private boolean openMigration = false;

    /**
     * spring注册开关
     */
    @SuppressWarnings("checkstyle:RegexpSingleLine")
    private boolean enableSpringRegister = false;

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

    public boolean isEnableSpringRegister() {
        return enableSpringRegister;
    }

    @SuppressWarnings("checkstyle:RegexpSingleLine")
    public void setEnableSpringRegister(boolean enableSpringRegister) {
        this.enableSpringRegister = enableSpringRegister;
    }

    public boolean isOpenMigration() {
        return openMigration;
    }

    @SuppressWarnings("checkstyle:RegexpSingleLine")
    public void setOpenMigration(boolean openMigration) {
        this.openMigration = openMigration;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    @SuppressWarnings("checkstyle:RegexpSingleLine")
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getAddress() {
        return address;
    }

    /**
     * 获取逗号分隔后的地址列表
     *
     * @return 地址列表
     */
    public List<String> getAddressList() {
        if (StringUtils.isBlank(address)) {
            return Collections.<String>emptyList();
        }
        return new ArrayList<String>(Arrays.asList(address.split(",")));
    }

    public void setAddress(String address) {
        this.address = address;
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
}
