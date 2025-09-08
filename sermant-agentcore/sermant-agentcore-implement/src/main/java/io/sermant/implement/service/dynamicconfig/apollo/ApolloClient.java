/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

import io.sermant.core.utils.StringUtils;
import io.sermant.implement.service.dynamicconfig.ConfigClient;
import io.sermant.implement.service.dynamicconfig.apollo.config.ApolloProperty;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * wraps the Apollo open apis and client to provides easier apis
 *
 * @author Chen Zhenyang
 * @since 2025-08-07
 */
public class ApolloClient implements ConfigClient {
    private static final Logger LOGGER = Logger.getLogger(ApolloClient.class.getName());
    private static final String PUBLISH_COMMENT_FORMAT =
            "publish from sermant, operate_type=%s user=%s app=%s env=%s cluster=%s time=%s";
    private final ApolloOpenApiClient apolloOpenApiClient;

    // patterns of fuzzy query
    private final Map<String, Pattern> patternMap = new ConcurrentHashMap<>();

    /**
     * constructor
     */
    public ApolloClient() {
        apolloOpenApiClient = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(ApolloProperty.getAdminUrl())
                .withToken(ApolloProperty.getToken())
                .build();

        // simple call to initialize apollo client config.
        ConfigService.getAppConfig();
    }

    @Override
    public String getConfig(String key, String group) {
        Config config = ConfigService.getConfig(group);
        return config.getProperty(key, "");
    }

    @Override
    public Map<String, List<String>> getConfigList(String key, String group, boolean accurateFlag) {
        Map<String, List<String>> configList = new HashMap<>();
        try {
            if (!accurateFlag) {
                List<OpenNamespaceDTO> namespaces = apolloOpenApiClient.getNamespaces(ApolloProperty.getAppId(),
                        ApolloProperty.getEnv(), ApolloProperty.getCluster(), true);
                Pattern groupPattern = patternMap.computeIfAbsent(group, patternKey -> Pattern.compile(group));
                Pattern keyPattern = patternMap.computeIfAbsent(key, patternKey -> Pattern.compile(key));
                for (OpenNamespaceDTO namespace : namespaces) {
                    if (!groupPattern.matcher(namespace.getNamespaceName()).matches()) {
                        continue;
                    }
                    List<String> list = new ArrayList<>();
                    for (OpenItemDTO item : namespace.getItems()) {
                        if (StringUtils.isNoneBlank(key) && !keyPattern.matcher(item.getKey()).matches()) {
                            continue;
                        }
                        list.add(item.getKey());
                    }
                    if (!list.isEmpty()) {
                        configList.put(namespace.getNamespaceName(), list);
                    }
                }
            } else {
                if (key == null || key.isEmpty()) {
                    OpenNamespaceDTO namespace = apolloOpenApiClient.getNamespace(ApolloProperty.getAppId(),
                            ApolloProperty.getEnv(), ApolloProperty.getCluster(), group, true);
                    if (namespace == null) {
                        return Collections.EMPTY_MAP;
                    }
                    List<OpenItemDTO> items = namespace.getItems();
                    List<String> list = items.stream().map(OpenItemDTO::getKey).collect(Collectors.toList());
                    configList.put(group, list);
                    return configList;
                }
                OpenItemDTO item = apolloOpenApiClient.getItem(ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                        ApolloProperty.getCluster(), group, key);
                if (item == null) {
                    return Collections.EMPTY_MAP;
                }
                List<String> list = new ArrayList<>();
                list.add(item.getKey());
                configList.put(group, list);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    String.format(Locale.ROOT, "Exception in querying configuration list of group:%s", group), e);
        }
        return configList;
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        try {
            ensureNamespace(group);
            OpenItemDTO openItemDto = new OpenItemDTO();
            openItemDto.setKey(key);
            openItemDto.setValue(content);
            openItemDto.setDataChangeCreatedBy(ApolloProperty.getUser());
            openItemDto.setDataChangeLastModifiedBy(ApolloProperty.getUser());
            apolloOpenApiClient.createOrUpdateItem(ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                    ApolloProperty.getCluster(), group, openItemDto);
            String comment = String.format(Locale.ROOT, PUBLISH_COMMENT_FORMAT,
                    String.format(Locale.ROOT, "publish %s %s", group, key),
                    ApolloProperty.getUser(), ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                    ApolloProperty.getCluster(), LocalDateTime.now());
            publishApollo(group, String.format(Locale.ROOT, "publish %s %s", group, key), comment);
            return true;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ROOT,
                    "Exception in publishing config group:%s key:%s", group, key), e);
            return false;
        }
    }

    /**
     * if namespace doesn't exist, create one
     *
     * @param namespace namespace of apollo
     */
    private void ensureNamespace(String namespace) {
        if (existsNamespace(namespace)) {
            return;
        }
        try {
            OpenAppNamespaceDTO ns = new OpenAppNamespaceDTO();
            ns.setAppId(ApolloProperty.getAppId());
            ns.setName(namespace);
            ns.setPublic(false);
            ns.setComment(String.format(Locale.ROOT, "Namespace:%s is created by sermant", namespace));
            ns.setDataChangeCreatedBy(ApolloProperty.getUser());
            apolloOpenApiClient.createAppNamespace(ns);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ApolloOpenApiException) {
                ApolloOpenApiException apolloException = (ApolloOpenApiException) e.getCause();
                if (apolloException.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST
                        || apolloException.getStatus() == HttpURLConnection.HTTP_CONFLICT) {
                    LOGGER.info(String.format(Locale.ROOT, "Namespace %s is already created", namespace));
                }
            }
            LOGGER.log(Level.SEVERE, String.format(Locale.ROOT, "Exception in creating namespace:%s", namespace), e);
            throw e;
        }
    }

    private boolean existsNamespace(String namespace) {
        try {
            apolloOpenApiClient.getNamespace(ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                    ApolloProperty.getCluster(), namespace);
            return true;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ApolloOpenApiException
                    && ((ApolloOpenApiException) e.getCause()).getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            }
            LOGGER.log(Level.SEVERE, String.format(Locale.ROOT, "Exception in creating namespace:%s", namespace), e);
            throw e;
        }
    }

    @Override
    public boolean removeConfig(String key, String group) {
        try {
            apolloOpenApiClient.removeItem(ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                    ApolloProperty.getCluster(), group, key, ApolloProperty.getUser());

            String comment = String.format(Locale.ROOT, PUBLISH_COMMENT_FORMAT,
                    String.format(Locale.ROOT, "remove %s %s", group, key),
                    ApolloProperty.getUser(), ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                    ApolloProperty.getCluster(), LocalDateTime.now());
            publishApollo(group, String.format(Locale.ROOT, "remove %s %s", group, key), comment);
            return true;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ApolloOpenApiException
                    && ((ApolloOpenApiException) e.getCause()).getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            }
            LOGGER.log(Level.SEVERE, String.format(Locale.ROOT, "Exception in removing config group:%s key:%s",
                    group, key), e);
            return false;
        }
    }

    private void publishApollo(String group, String title, String comment) {
        // if double check is required, the publish operation must be performed by a user different from the editor
        if (ApolloProperty.isDoubleCheck()) {
            LOGGER.info(String.format(Locale.ROOT, "Modification of group:%s needs double check.Details:%s",
                    group, comment));
            return;
        }
        try {
            NamespaceReleaseDTO namespaceReleaseDto = new NamespaceReleaseDTO();
            namespaceReleaseDto.setReleasedBy(ApolloProperty.getUser());
            namespaceReleaseDto.setReleaseTitle(title);
            namespaceReleaseDto.setReleaseComment(comment);
            apolloOpenApiClient.publishNamespace(ApolloProperty.getAppId(), ApolloProperty.getEnv(),
                    ApolloProperty.getCluster(), group, namespaceReleaseDto);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ROOT, "Failed to release namespace:%s", group), e);
            throw e;
        }
    }

    @Override
    public boolean isConnect() {
        return isAvailable(ApolloProperty.getUrl()) && isAvailable(ApolloProperty.getAdminUrl());
    }

    /**
     * check url is available
     *
     * @param configServerUrl url to server
     * @return check if url is available
     */
    public boolean isAvailable(String configServerUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(configServerUrl + "/health");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(ApolloProperty.getConnectTimeout());
            connection.setReadTimeout(ApolloProperty.getReadTimeout());
            int responseCode = getResponse(connection);
            return responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE;
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * get http response code
     *
     * @param connection http connection
     * @return http code
     * @throws IOException http connection exception
     */
    int getResponse(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode();
    }

    /**
     * close the client
     */
    public void close() {
    }

    /**
     * add group listener
     *
     * @param group valid namespace
     * @param apolloListener listener
     * @return check if add listener successfully
     */
    public boolean addGroupListener(String group, ConfigChangeListener apolloListener) {
        Config config = ConfigService.getConfig(group);
        config.addChangeListener(apolloListener);
        return true;
    }

    /**
     * listen to interested key only
     *
     * @param key apollo key
     * @param group apollo valid namespace
     * @param apolloListener listener
     * @return add result
     */
    public boolean addConfigListener(String key, String group, ConfigChangeListener apolloListener) {
        Config config = ConfigService.getConfig(group);
        HashSet<String> interestedKeys = new HashSet<>();
        interestedKeys.add(key);
        config.addChangeListener(apolloListener, interestedKeys);
        return true;
    }

    /**
     * remove config listener on the namespace
     *
     * @param group valid namespace
     * @param listener listener
     * @return check if remove listener successfully
     */
    public boolean removeConfigListener(String group, ConfigChangeListener listener) {
        Config config = ConfigService.getConfig(group);
        return config.removeChangeListener(listener);
    }
}
