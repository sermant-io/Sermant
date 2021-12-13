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
 *
 * Config for this DynamicConfig Module
 *
 */
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    static Config singleInst;

    public synchronized static Config getInstance()
    {
        if ( singleInst == null ) {
            logger.warn("Config failed to init from configfile. Load config from hardcode.");
            singleInst = new Config();
        }
        return singleInst;
    }

    public static int getTimeout_value() {
        return getInstance().timeout_value;
    }

    public static String getDefaultGroup() {
        return getInstance().default_group;
    }

    public static String getZookeeperUri() {
        return getInstance().zookeeper_uri;
    }

    public static DynamicConfigType getDynamic_config_type() {
        return getInstance().dynamic_config_type;
    }


    protected void setTimeout_value(int timeout_value) {
        this.timeout_value = timeout_value;
    }

    protected void setDefault_group(String default_group) {
        this.default_group = default_group;
    }

    protected void setZookeeper_uri(String zookeeper_uri) {
        this.zookeeper_uri = zookeeper_uri;
    }

    protected void setDynamic_config_type(DynamicConfigType dynamicConfigType) {
        this.dynamic_config_type = dynamicConfigType;
    }

    protected int timeout_value = 30000;

    protected String default_group = "sermant";

    protected String zookeeper_uri = "zookeeper://127.0.0.1:2181";

    protected DynamicConfigType dynamic_config_type = DynamicConfigType.ZOO_KEEPER; //DynamicConfigType.ZOO_KEEPER;

    /**
     * kie配置地址
     */
    protected String kie_url = "http://127.0.0.1:30110";

    /**
     * 默认kie的命名空间
     */
    protected String project = "default";

    public String getKie_url() {
        return kie_url;
    }

    public void setKie_url(String kie_url) {
        this.kie_url = kie_url;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
