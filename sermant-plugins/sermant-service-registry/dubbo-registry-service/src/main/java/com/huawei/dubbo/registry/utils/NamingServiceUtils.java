/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.dubbo.registry.utils;

import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.config.PropertyKeyConst;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos注册naming服务
 *
 * @since 2022-10-25
 */
public class NamingServiceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String BACKUP_KEY = "backup";

    private NamingServiceUtils() {
    }

    /**
     * 构建naming服务
     *
     * @param parameters url
     * @param registerConfig registerConfig
     * @return naming服务
     * @throws IllegalStateException 创建namingService异常
     */
    public static NamingService buildNamingService(Map<String, String> parameters, NacosRegisterConfig registerConfig) {
        Properties nacosProperties = buildNacosProperties(parameters, registerConfig);
        try {
            return NacosFactory.createNamingService(nacosProperties);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "create namingService failed: {%s}",
                    e.getErrMsg()), e);
            throw new IllegalStateException(e);
        }
    }

    private static Properties buildNacosProperties(Map<String, String> parameters, NacosRegisterConfig registerConfig) {
        Properties properties = new Properties();
        setServerAddr(parameters.get(BACKUP_KEY), properties, registerConfig);
        setProperties(parameters, properties, registerConfig);
        return properties;
    }

    private static void setServerAddr(String backup, Properties properties, NacosRegisterConfig registerConfig) {
        StringBuilder serverAddrBuilder = new StringBuilder();
        serverAddrBuilder.append(registerConfig.getAddress());
        if (!StringUtils.isEmpty(backup)) {
            serverAddrBuilder.append(',').append(backup);
        }
        String serverAddr = serverAddrBuilder.toString();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
    }

    private static void setProperties(Map<String, String> parameters, Properties properties,
        NacosRegisterConfig registerConfig) {
        properties.putAll(parameters);
        if (!StringUtils.isEmpty(registerConfig.getUsername())) {
            properties.put(PropertyKeyConst.USERNAME, registerConfig.getUsername());
        }
        if (!StringUtils.isEmpty(registerConfig.getPassword())) {
            properties.put(PropertyKeyConst.PASSWORD, registerConfig.getPassword());
        }
        if (!StringUtils.isEmpty(registerConfig.getLogName())) {
            properties.put(PropertyKeyConst.NACOS_NAMING_LOG_NAME, registerConfig.getLogName());
        }
        if (!StringUtils.isEmpty(registerConfig.getNamingLoadCacheAtStart())) {
            properties.put(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START, registerConfig.getNamingLoadCacheAtStart());
        }
    }
}
