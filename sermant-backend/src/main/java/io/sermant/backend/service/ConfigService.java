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

import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.common.conf.DynamicConfig;
import io.sermant.backend.entity.config.ConfigCenterType;
import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.ConfigServerInfo;
import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.template.PageTemplateInfo;
import io.sermant.backend.util.AesUtil;
import io.sermant.implement.service.dynamicconfig.ConfigClient;
import io.sermant.implement.service.dynamicconfig.kie.client.ClientUrlManager;
import io.sermant.implement.service.dynamicconfig.kie.client.kie.KieClient;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * ZK authorization separator
     */
    private static final char ZK_AUTH_SEPARATOR = ':';

    private static final String SCHEME = "digest";

    /**
     * Match whether variables are included, and if so, replace them with wildcard characters for subsequent queries
     */
    private final Pattern variablePattern = Pattern.compile("\\$\\{\\s*\\w+\\s*\\}");

    /**
     * Regular expression map, storing regular expressions for configuration items
     */
    private final Map<String, Pattern> patternMap = new ConcurrentHashMap<>();

    @Resource
    private DynamicConfig dynamicConfig;

    private ConfigClient configClient;

    @Resource
    private PageTemplateService pageTemplateService;

    /**
     * Query Configuration List
     *
     * @param request Query criteria for configuration list
     * @return Configuration List
     */
    public Result<List<ConfigInfo>> getConfigList(ConfigInfo request) {
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client == null || !client.isConnect()) {
            return new Result<>(ResultCodeType.CONNECT_FAIL);
        }
        Result<PageTemplateInfo> templateInfoResult = pageTemplateService.getTemplate(request.getPluginType());
        if (!StringUtils.equals(CommonConst.COMMON_TEMPLATE, request.getPluginType())
                && !templateInfoResult.isSuccess()) {
            return new Result<>(ResultCodeType.FAIL.getCode(), "Invalid plugin name.");
        }
        PageTemplateInfo template = templateInfoResult.getData();
        List<ConfigInfo> configInfoList = getConfigInfos(request, template, client);
        return new Result<>(ResultCodeType.SUCCESS.getCode(), null, configInfoList);
    }

    /**
     * Query Configuration List
     *
     * @param request Query criteria for configuration list
     * @param template template information for configuration management page
     * @param client Configuration Center Client
     * @return Configuration List
     */
    private List<ConfigInfo> getConfigInfos(ConfigInfo request, PageTemplateInfo template, ConfigClient client) {
        List<String> keyRules;
        if (StringUtils.isEmpty(request.getKeyRule())) {
            keyRules = StringUtils.equals(CommonConst.COMMON_TEMPLATE, request.getPluginType())
                    ? Collections.singletonList(CommonConst.PATTERN_WILDCARD) : template.getKeyRule();
        } else {
            keyRules = Collections.singletonList(request.getKeyRule());
        }
        List<String> groupRules = StringUtils.isEmpty(request.getGroupRule())
                ? template.getGroupRule() : Collections.singletonList(request.getGroupRule());
        Set<ConfigInfo> configInfoSet = new HashSet<>();
        groupRules.forEach(groupRule -> {
            String groupPattern = client instanceof NacosClient
                    ? rebuildGroup(replaceVariableWithWildcard(groupRule, CommonConst.QUERY_WILDCARD))
                    : replaceVariableWithWildcard(groupRule, CommonConst.PATTERN_WILDCARD);
            Map<String, List<String>> configMap = client.getConfigList(StringUtils.EMPTY, groupPattern,
                    request.isExactMatchFlag());
            fillMatchedConfigItems(request, groupRule, configMap, keyRules, configInfoSet);
        });
        return new ArrayList<>(configInfoSet);
    }

    /**
     * Fill matched configuration item information
     *
     * @param request Query criteria for configuration list
     * @param groupRule Generation rules for configuration item groups
     * @param configMap Configuration information queried from the configuration center
     * @param keyRules Generation rules for configuration item keys
     * @param configInfoSet Configuration List
     */
    private void fillMatchedConfigItems(ConfigInfo request, String groupRule, Map<String, List<String>> configMap,
                                        List<String> keyRules, Set<ConfigInfo> configInfoSet) {
        configMap.forEach((group, value) -> value.forEach(configItem -> {
            Optional<String> firstMatch = keyRules.stream().filter(rule -> {
                String keyPattern = replaceVariableWithWildcard(rule, CommonConst.PATTERN_WILDCARD);
                Pattern pattern = patternMap.computeIfAbsent(keyPattern, key -> Pattern.compile(keyPattern));
                return pattern.matcher(configItem).matches();
            }).findFirst();
            if (firstMatch.isPresent()) {
                ConfigInfo configInfo = new ConfigInfo(configItem, convertGroup(group), firstMatch.get(),
                        groupRule, request.getNamespace());
                configInfo.setPluginType(request.getPluginType());
                configInfoSet.add(configInfo);
            }
        }));
    }

    /**
     * Query Configuration
     *
     * @param request Query criteria for configuration
     * @return Configuration
     */
    public Result<ConfigInfo> getConfig(ConfigInfo request) {
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client == null || !client.isConnect()) {
            return new Result<>(ResultCodeType.CONNECT_FAIL);
        }
        String content = client.getConfig(request.getKey(), rebuildGroup(request.getGroup()));
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
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client == null || !client.isConnect()) {
            return new Result<>(ResultCodeType.CONNECT_FAIL);
        }
        boolean result = client.publishConfig(request.getKey(), rebuildGroup(request.getGroup()),
                request.getContent());
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
        ConfigClient client = getConfigClient(request.getNamespace());
        if (client == null || !client.isConnect()) {
            return new Result<>(ResultCodeType.CONNECT_FAIL);
        }
        boolean result = client.removeConfig(request.getKey(), rebuildGroup(request.getGroup()));
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
        String serverAddress = dynamicConfig.getServerAddress();
        int timeout = dynamicConfig.getTimeout();
        if (ConfigCenterType.ZOOKEEPER.name().equals(dynamicConfig.getDynamicConfigType())) {
            configClient = createZookeeperClient(serverAddress, timeout);
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

    private ZooKeeperClient createZookeeperClient(String serverAddress, int timeout) {
        Watcher watcher = new Watcher() {
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
        return client;
    }

    /**
     * Get Configuration Center Client
     *
     * @return Configuration Center Client
     */
    public ConfigClient getConfigClient() {
        return getConfigClient(null);
    }

    /**
     * Get Configuration Center Client
     *
     * @param namespace Configuration namespace
     * @return Configuration Center Client
     */
    public ConfigClient getConfigClient(String namespace) {
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
     * Rebuild the valid group name.
     *
     * @param group group name
     * @return valid group
     */
    private String rebuildGroup(String group) {
        if (configClient instanceof NacosClient) {
            return NacosUtils.rebuildGroup(group);
        }
        return group;
    }

    /**
     * convert the group name to nacos group
     *
     * @param group group name
     * @return valid group
     */
    public String convertGroup(String group) {
        if (configClient instanceof NacosClient) {
            return NacosUtils.convertGroup(group);
        }
        return group;
    }

    /**
     * Replace variables with wildcards for matching during queries.
     *
     * @param rule The generation rules for keys or groups.
     * @param wildcard Wildcard characters
     * @return The regular expression for key or group.
     */
    private String replaceVariableWithWildcard(String rule, String wildcard) {
        Matcher matcher = variablePattern.matcher(rule);
        return matcher.replaceAll(wildcard);
    }
}
