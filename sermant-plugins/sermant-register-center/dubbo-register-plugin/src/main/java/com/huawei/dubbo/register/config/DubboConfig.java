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

package com.huawei.dubbo.register.config;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * dubbo配置
 *
 * @author provenceee
 * @date 2021/12/23
 */
public class DubboConfig {
    /**
     * 命名空间
     */
    @Value("${servicecomb.service.project:default}")
    private String project;

    /**
     * 应用名
     */
    @Value("${servicecomb.service.application:default}")
    private String application;

    /**
     * 微服务版本
     */
    @Value("${servicecomb.service.version:0.0.0}")
    private String version;

    /**
     * 环境，可选值：development, testing, acceptance, production
     */
    @Value("${servicecomb.service.environment:development}")
    private String environment;

    /**
     * 注册中心地址
     */
    @Value("${servicecomb.service.address:http://127.0.0.1:30100}")
    private List<String> address;

    /**
     * 心跳发送间隔（单位：秒）
     */
    @Value("${servicecomb.service.heartbeatInterval:15}")
    private int heartbeatInterval;

    /**
     * 心跳重试次数
     */
    @Value("${servicecomb.service.heartbeatRetryTimes:3}")
    private int heartbeatRetryTimes;

    /**
     * 拉取实例时间间隔
     */
    @Value("${servicecomb.service.pullInterval:15}")
    private int pullInterval;

    /**
     * 是否开启迁移功能
     */
    @Value("${servicecomb.service.openMigration:false}")
    private boolean openMigration;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
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

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
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

    public boolean isOpenMigration() {
        return openMigration;
    }

    public void setOpenMigration(boolean openMigration) {
        this.openMigration = openMigration;
    }
}