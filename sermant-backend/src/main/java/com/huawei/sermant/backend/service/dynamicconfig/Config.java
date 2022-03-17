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

package com.huawei.sermant.backend.service.dynamicconfig;

import com.huawei.sermant.backend.service.dynamicconfig.service.DynamicConfigType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config for this DynamicConfig Module
 *
 *  @author yangyi
 *  @since 2021-12-10
 */
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    static Config singleInst;

    private static final int DEFAULT_TIMEOUT_TIME = 30000;

    /**
     * 超时时间
     */
    protected int timeoutValue = DEFAULT_TIMEOUT_TIME;

    protected String defaultGroup = "sermant";

    protected String zookeeperUri = "zookeeper://127.0.0.1:2181";

    protected DynamicConfigType dynamicConfigType = DynamicConfigType.ZOO_KEEPER;

    /**
     * kie配置地址
     */
    protected String kieUrl = "http://127.0.0.1:30110";

    /**
     * 默认kie的命名空间
     */
    protected String project = "default";

    /**
     * 获取配置
     *
     * @return 配置
     */
    public static synchronized Config getInstance() {
        if (singleInst == null) {
            logger.warn("Config failed to init from configfile. Load config from hardcode.");
            singleInst = new Config();
        }
        return singleInst;
    }

    public static int getTimeout_value() {
        return getInstance().timeoutValue;
    }

    public static String getDefaultGroup() {
        return getInstance().defaultGroup;
    }

    public static String getZookeeperUri() {
        return getInstance().zookeeperUri;
    }

    public static DynamicConfigType getDynamic_config_type() {
        return getInstance().dynamicConfigType;
    }

    /**
     * 设置超时时间
     *
     * @param time 超时时间
     */
    protected void setTimeoutValue(int time) {
        this.timeoutValue = time;
    }

    /**
     * 设置配置组
     *
     * @param defaultGroup group
     */
    protected void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    /**
     * 设置zk uri
     *
     * @param uri 地址
     */
    protected void setZookeeperUri(String uri) {
        this.zookeeperUri = uri;
    }

    /**
     * 设置动态配置类型
     *
     * @param dynamicConfigType 动态配置类型
     */
    protected void setDynamicConfigType(DynamicConfigType dynamicConfigType) {
        this.dynamicConfigType = dynamicConfigType;
    }

    public String getKieUrl() {
        return kieUrl;
    }

    public void setKieUrl(String url) {
        this.kieUrl = url;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
