/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.service;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.auth.impl.NacosAuthLoginConstant;

import io.sermant.backend.common.conf.DynamicConfig;
import io.sermant.backend.entity.config.ConfigCenterType;
import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.ConfigServerInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.handler.config.PluginConfigHandler;
import io.sermant.backend.util.AesUtil;
import io.sermant.implement.service.dynamicconfig.ConfigClient;
import io.sermant.implement.service.dynamicconfig.kie.client.ClientUrlManager;
import io.sermant.implement.service.dynamicconfig.kie.client.kie.KieClient;
import io.sermant.implement.service.dynamicconfig.kie.constants.KieConstants;
import io.sermant.implement.service.dynamicconfig.nacos.NacosClient;
import io.sermant.implement.service.dynamicconfig.nacos.NacosUtils;
import io.sermant.implement.service.dynamicconfig.zookeeper.ZooKeeperClient;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Configure Service
 *
 * @author zhp
 * @since 2024-05-16
 */
@Service
public class ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    private static final Map<String, ConfigClient> CONFIG_CLIENT_MAP = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    /**
     * ZK authorization separator
     */
    private static final char ZK_AUTH_SEPARATOR = ':';

    private static final String SCHEME = "digest";

    @Resource
    private DynamicConfig dynamicConfig;

    private ConfigClient configClient;

    private Watcher watcher;

    /**
     * Query Configuration List
     *
     * @param request Query criteria for configuration list
     * @param pluginType Plugin type
     * @param exactMatchFlag Identification of exact match
     * @return Configuration List
     */
    public Result<List<ConfigInfo>> getConfigList(ConfigInfo request, PluginType pluginType, boolean exactMatchFlag) {
        Result<?> result = checkConnection(request);
        if (!result.isSuccess()) {
            return new Result<>(result.getCode(), result.getMessage());
        }
        String requestGroup = request.getGroup();
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client instanceof NacosClient) {
            requestGroup = NacosUtils.rebuildGroup(requestGroup);
        }
        Map<String, List<String>> configMap = client.getConfigList(request.getKey(), requestGroup, exactMatchFlag);
        List<ConfigInfo> configInfoList = new ArrayList<>();
        PluginConfigHandler handler = pluginType.getHandler();
        for (Map.Entry<String, List<String>> entry : configMap.entrySet()) {
            String group = entry.getKey();
            if (client instanceof NacosClient) {
                group = NacosUtils.convertGroup(group);
            } else if (client instanceof KieClient) {
                group = group.replace(KieConstants.CONNECTOR, KieConstants.SEPARATOR);
                group = group.replace(KieConstants.DEFAULT_LABEL_PRE, StringUtils.EMPTY);
            }
            if (!exactMatchFlag && !handler.verifyConfigurationGroup(group)) {
                continue;
            }
            for (String configKey : entry.getValue()) {
                if (!exactMatchFlag && !handler.verifyConfigurationKey(configKey)) {
                    continue;
                }
                ConfigInfo configInfo = handler.parsePluginInfo(configKey, group);
                if (!exactMatchFlag && !handler.filterConfiguration(request, configInfo)) {
                    continue;
                }
                configInfo.setNamespace(request.getNamespace());
                configInfoList.add(configInfo);
            }
        }
        return new Result<>(ResultCodeType.SUCCESS.getCode(), null, configInfoList);
    }

    /**
     * Query Configuration
     *
     * @param request Query criteria for configuration
     * @return Configuration
     */
    public Result<ConfigInfo> getConfig(ConfigInfo request) {
        Result<Boolean> result = checkConnection(request);
        if (!result.isSuccess()) {
            return new Result<>(result.getCode(), result.getMessage());
        }
        String group = request.getGroup();
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client instanceof NacosClient) {
            group = NacosUtils.rebuildGroup(request.getGroup());
        }
        String content = client.getConfig(request.getKey(), group);
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setGroup(request.getGroup());
        configInfo.setKey(request.getKey());
        configInfo.setContent(content);
        return new Result<>(ResultCodeType.SUCCESS.getCode(), null, configInfo);
    }

    /**
     * publish configuration information
     *
     * @param request Configuration information
     * @return The result of publishing configuration information
     */
    public Result<Boolean> publishConfig(ConfigInfo request) {
        Result<Boolean> checkResult = checkConnection(request);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        String group = request.getGroup();
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client instanceof NacosClient) {
            group = NacosUtils.rebuildGroup(request.getGroup());
        }
        boolean result = client.publishConfig(request.getKey(), group, request.getContent());
        if (result) {
            return new Result<>(ResultCodeType.SUCCESS.getCode(), null, true);
        }
        return new Result<>(ResultCodeType.MODIFY_FAIL.getCode(), null, false);
    }

    /**
     * Delete configuration information
     *
     * @param request Configuration information
     * @return The result of deleting configuration information
     */
    public Result<Boolean> deleteConfig(ConfigInfo request) {
        Result<Boolean> checkResult = checkConnection(request);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        String group = request.getGroup();
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client instanceof NacosClient) {
            group = NacosUtils.rebuildGroup(request.getGroup());
        }
        boolean result = client.removeConfig(request.getKey(), group);
        if (result) {
            return new Result<>(ResultCodeType.SUCCESS.getCode(), null, true);
        }
        return new Result<>(ResultCodeType.DELETE_FAIL.getCode(), null, false);
    }

    /**
     * Initialize Configuration Center Client
     */
    @PostConstruct
    public void init() {
        if (!dynamicConfig.isEnable()) {
            return;
        }
        EXECUTOR_SERVICE.scheduleAtFixedRate(this::reConnection, dynamicConfig.getConnectTimeout(),
                dynamicConfig.getConnectTimeout(), TimeUnit.MILLISECONDS);
        String serverAddress = dynamicConfig.getServerAddress();
        int timeout = dynamicConfig.getTimeout();
        if (ConfigCenterType.ZOOKEEPER.name().equals(dynamicConfig.getDynamicConfigType())) {
            watcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.Expired) {
                        configClient = new ZooKeeperClient(serverAddress, timeout, this);
                    }
                }
            };
            ZooKeeperClient client = new ZooKeeperClient(serverAddress, timeout, watcher);
            if (dynamicConfig.isEnableAuth() && StringUtils.isNotBlank(dynamicConfig.getUserName())
                    && StringUtils.isNotBlank(dynamicConfig.getPassword())
                    && StringUtils.isNotBlank(dynamicConfig.getSecretKey())) {
                String authInfo = dynamicConfig.getUserName() + ZK_AUTH_SEPARATOR
                        + AesUtil.decrypt(dynamicConfig.getSecretKey(), dynamicConfig.getPassword()).orElse(null);
                client.addAuthInfo(SCHEME, authInfo.getBytes(StandardCharsets.UTF_8));
            }
            configClient = client;
            return;
        }
        if (ConfigCenterType.NACOS.name().equals(dynamicConfig.getDynamicConfigType())) {
            configClient = createNacosClient(dynamicConfig.getNamespace());
            return;
        }
        if (ConfigCenterType.KIE.name().equals(dynamicConfig.getDynamicConfigType())) {
            configClient = new KieClient(new ClientUrlManager(serverAddress), "default", timeout);
        }
    }

    /**
     * Get Configuration Center Client
     *
     * @param namespace Configuration namespace
     * @return Configuration Center Client
     */
    private ConfigClient getConfigClient(String namespace) {
        if (!ConfigCenterType.NACOS.name().equals(dynamicConfig.getDynamicConfigType())
                || StringUtils.isEmpty(namespace)) {
            return configClient;
        }
        ConfigClient client = CONFIG_CLIENT_MAP.get(namespace);
        if (client == null) {
            return createNacosClient(namespace);
        }
        return client;
    }

    private ConfigClient createNacosClient(String namespace) {
        Properties properties = createProperties(namespace);
        ConfigClient client = null;
        try {
            client = new NacosClient(properties);
            CONFIG_CLIENT_MAP.put(namespace, client);
        } catch (NacosException e) {
            LOGGER.error("Nacos connection exception", e);
        }
        return client;
    }

    private Properties createProperties(String namespace) {
        Properties properties = new Properties();
        if (dynamicConfig.isEnableAuth() && StringUtils.isNotBlank(dynamicConfig.getUserName())
                && StringUtils.isNotBlank(dynamicConfig.getPassword())
                && StringUtils.isNotBlank(dynamicConfig.getSecretKey())) {
            String userName = dynamicConfig.getUserName();
            String password = AesUtil.decrypt(dynamicConfig.getSecretKey(), dynamicConfig.getPassword())
                    .orElse(null);
            properties.setProperty(PropertyKeyConst.USERNAME, userName);
            properties.setProperty(PropertyKeyConst.PASSWORD, password);
        }
        properties.setProperty(NacosAuthLoginConstant.SERVER, dynamicConfig.getServerAddress());
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, dynamicConfig.getServerAddress());
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        properties.setProperty(PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT,
                String.valueOf(dynamicConfig.getTimeout()));
        return properties;
    }

    /**
     * Verify if a connection is established
     *
     * @param request Request Information
     * @return Configuration Center Client
     */
    public Result<Boolean> checkConnection(ConfigInfo request) {
        if (!dynamicConfig.isEnable()) {
            return new Result<>(ResultCodeType.NOT_ENABLE.getCode(), ResultCodeType.NOT_ENABLE.getMessage());
        }
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client == null || !client.isConnect()) {
            return new Result<>(ResultCodeType.CONNECT_FAIL.getCode(), ResultCodeType.CONNECT_FAIL.getMessage());
        }
        return new Result<>(ResultCodeType.SUCCESS.getCode(), ResultCodeType.SUCCESS.getMessage());
    }

    /**
     * get configuration center information
     *
     * @return Configuration Center Information
     */
    public Result<ConfigServerInfo> getConfigurationCenter() {
        ConfigServerInfo configServerInfo = new ConfigServerInfo();
        configServerInfo.setServerAddress(dynamicConfig.getServerAddress());
        configServerInfo.setUserName(dynamicConfig.getUserName());
        configServerInfo.setDynamicConfigType(dynamicConfig.getDynamicConfigType());
        configServerInfo.setNamespace(dynamicConfig.getNamespace());
        return new Result<>(ResultCodeType.SUCCESS.getCode(), ResultCodeType.SUCCESS.getMessage(), configServerInfo);
    }

    /**
     * Verify if the client is disconnected, and reconnect when disconnected
     */
    public void reConnection() {
        if (ConfigCenterType.ZOOKEEPER.name().equals(dynamicConfig.getDynamicConfigType())
                && !configClient.isConnect()) {
            configClient = new ZooKeeperClient(dynamicConfig.getServerAddress(), dynamicConfig.getTimeout(), watcher);
        }
    }
}
