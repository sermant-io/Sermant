/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.config;

/**
 * 流控遗留代码配置保留
 *
 * @author zhouss
 * @since 2022-03-22
 */
public class FlowControlLegacyConfig {
    /**
     * sentinel的版本
     */
    private String sentinelVersion = "1.8.0";

    /**
     * 流控插件zk地址
     */
    private String zookeeperAddress = "127.0.0.1:2181";

    /**
     * 流控相关配置在zk中的node
     */
    private String flowControlZookeeperPath = "/sentinel_rule_config";

    /**
     * 开发环境配置文件，默认为dev
     */
    private String configProfileActive = "dev";

    /**
     * 配置流控插件
     */
    private String configZookeeperPath = "/flowcontrol_plugin_config";

    /**
     * 对接的配置中心类型，当前支持zookeeper、servicecomb-kie两种类型 默认为对接zookeeper，对接kie时请改为servicecomb-kie
     */
    private String configCenterType = "zookeeper";

    /**
     * 是否使用插件自身的url地址 该配置仅zookeeper生效 默认使用的是配置中心地址
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    private boolean needUseSelfUrl = false;

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public String getFlowControlZookeeperPath() {
        return flowControlZookeeperPath;
    }

    public void setFlowControlZookeeperPath(String flowControlZookeeperPath) {
        this.flowControlZookeeperPath = flowControlZookeeperPath;
    }

    public String getConfigProfileActive() {
        return configProfileActive;
    }

    public void setConfigProfileActive(String configProfileActive) {
        this.configProfileActive = configProfileActive;
    }

    public String getConfigZookeeperPath() {
        return configZookeeperPath;
    }

    public void setConfigZookeeperPath(String configZookeeperPath) {
        this.configZookeeperPath = configZookeeperPath;
    }

    public String getConfigCenterType() {
        return configCenterType;
    }

    public void setConfigCenterType(String configCenterType) {
        this.configCenterType = configCenterType;
    }

    public boolean isNeedUseSelfUrl() {
        return needUseSelfUrl;
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void setNeedUseSelfUrl(boolean needUseSelfUrl) {
        this.needUseSelfUrl = needUseSelfUrl;
    }

    public String getSentinelVersion() {
        return sentinelVersion;
    }

    public void setSentinelVersion(String sentinelVersion) {
        this.sentinelVersion = sentinelVersion;
    }
}
