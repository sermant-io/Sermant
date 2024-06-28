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

package io.sermant.implement.service.dynamicconfig.kie.client.kie;

import io.sermant.implement.service.dynamicconfig.ConfigClient;
import io.sermant.implement.service.dynamicconfig.common.DynamicConstants;
import io.sermant.implement.service.dynamicconfig.kie.client.AbstractClient;
import io.sermant.implement.service.dynamicconfig.kie.client.ClientUrlManager;
import io.sermant.implement.service.dynamicconfig.kie.client.http.HttpClient;
import io.sermant.implement.service.dynamicconfig.kie.client.http.HttpResult;
import io.sermant.implement.service.dynamicconfig.kie.constants.KieConstants;
import io.sermant.implement.utils.LabelGroupUtils;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Kie Client
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieClient extends AbstractClient implements ConfigClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class.getName());

    /**
     * Default version
     */
    private static final String ABSENT_REVISION = "0";

    private static final String KIE_API_TEMPLATE = "/v1/%s/kie/kv?";

    private final ResultHandler<KieResponse> defaultHandler = new ResultHandler.DefaultResultHandler();

    private String kieApi;

    /**
     * Kie client constructor
     *
     * @param clientUrlManager kie url manager
     * @param project namespace
     * @param timeout timeout
     */
    public KieClient(ClientUrlManager clientUrlManager, String project, int timeout) {
        this(clientUrlManager, null, project, timeout);
    }

    /**
     * Kie client constructor
     *
     * @param clientUrlManager kie url manager
     * @param httpClient HttpClient
     * @param project namespace
     * @param timeout timeout
     */
    public KieClient(ClientUrlManager clientUrlManager, HttpClient httpClient, String project, int timeout) {
        super(clientUrlManager, httpClient, timeout);
        kieApi = String.format(KIE_API_TEMPLATE, project);
    }

    /**
     * Set the kieApi to project
     *
     * @param project namespace
     */
    public void setProject(String project) {
        this.kieApi = String.format(KIE_API_TEMPLATE, project);
    }

    /**
     * Querying Kie Configuration
     *
     * @param request Kie request
     * @return KieResponse
     */
    public KieResponse queryConfigurations(KieRequest request) {
        return queryConfigurations(request, defaultHandler);
    }

    /**
     * Querying Kie Configuration
     *
     * @param request Kie request
     * @param responseHandler http result handler
     * @param <T> The converted target type
     * @return Response result
     */
    public <T> T queryConfigurations(KieRequest request, ResultHandler<T> responseHandler) {
        if (request == null || responseHandler == null) {
            return null;
        }
        final StringBuilder requestUrl = new StringBuilder().append(clientUrlManager.getUrl()).append(kieApi);
        requestUrl.append(formatNullString(request.getLabelCondition()))
                .append("&revision=")
                .append(formatNullString(request.getRevision()));
        if (request.isAccurateMatchLabel()) {
            requestUrl.append("&match=exact");
        }
        if (request.getKey() != null) {
            if (request.isAccurateMatchLabel()) {
                requestUrl.append("&key=").append(request.getKey());
            } else {
                requestUrl.append("&key=wildcard(*").append(request.getKey()).append("*)");
            }
        }
        if (request.getWait() != null) {
            requestUrl.append("&wait=").append(formatNullString(request.getWait())).append("s");
        }
        final HttpResult httpResult = httpClient.doGet(requestUrl.toString(), request.getRequestConfig());
        return responseHandler.handle(httpResult);
    }

    /**
     * Publish configuration
     *
     * @param key request key
     * @param labels labels
     * @param content configuration content
     * @param enabled configuration switch status
     * @return publish result
     */
    public boolean publishConfig(String key, Map<String, String> labels, String content, boolean enabled) {
        final Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        params.put("value", content);
        params.put("labels", labels);
        params.put("status", enabled ? "enabled" : "disabled");
        final HttpResult httpResult = this.httpClient.doPost(clientUrlManager.getUrl() + kieApi, params);
        return httpResult.getCode() == HttpStatus.SC_OK;
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        final Optional<String> keyIdOptional = this.getKeyId(key, group);
        if (keyIdOptional.isPresent()) {
            return this.doUpdateConfig(keyIdOptional.get(), content, true);
        }

        // If not exists, then publish
        final Map<String, String> labels = LabelGroupUtils.resolveGroupLabels(group);
        return this.publishConfig(key, labels, content, true);
    }

    /**
     * Update configuration
     *
     * @param keyId key id
     * @param content update content
     * @param enabled enable or not
     * @return update result
     */
    public boolean doUpdateConfig(String keyId, String content, boolean enabled) {
        final Map<String, Object> params = new HashMap<>();
        params.put("value", content);
        params.put("status", enabled ? "enabled" : "disabled");
        final HttpResult httpResult = this.httpClient.doPut(buildKeyIdUrl(keyId), params);
        return httpResult.getCode() == HttpStatus.SC_OK;
    }

    private String buildKeyIdUrl(String keyId) {
        return String.format(Locale.ENGLISH, "%s/%s",
                clientUrlManager.getUrl() + kieApi.substring(0, kieApi.length() - 1), keyId);
    }

    /**
     * Delete configuration
     *
     * @param keyId key id
     * @return delete result
     */
    public boolean doDeleteConfig(String keyId) {
        final HttpResult httpResult = this.httpClient.doDelete(buildKeyIdUrl(keyId));
        return httpResult.getCode() == HttpStatus.SC_OK;
    }

    private String formatNullString(String val) {
        if (val == null || val.trim().isEmpty()) {
            // When the version number is empty, the default version number is set to "0". When the version is
            // updated, the data is returned immediately to avoid blocking problems
            return ABSENT_REVISION;
        }
        return val;
    }

    @Override
    public String getConfig(String key, String group) {
        final KieResponse kieResponse = this.getKieResponse(key, group, true);
        if (kieResponse == null || kieResponse.getData() == null) {
            return DynamicConstants.EMPTY_STRING;
        }
        final List<KieConfigEntity> data = kieResponse.getData();
        for (KieConfigEntity entity : data) {
            return entity.getValue();
        }
        return DynamicConstants.EMPTY_STRING;
    }

    @Override
    public Map<String, List<String>> getConfigList(String key, String group, boolean exactMatchFlag) {
        final KieResponse kieResponse;
        String covertGroup = group.replace(KieConstants.SEPARATOR, KieConstants.CONNECTOR);
        if (exactMatchFlag) {
            kieResponse = getKieResponse(key, covertGroup, exactMatchFlag);
        } else {
            kieResponse = getKieResponse(key, null, exactMatchFlag);
        }
        if (kieResponse == null || kieResponse.getData() == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> result = new HashMap<>();
        for (KieConfigEntity entity : kieResponse.getData()) {
            if (exactMatchFlag) {
                List<String> configList = result.computeIfAbsent(LabelGroupUtils.createLabelGroup(entity.getLabels()),
                        configKey -> new ArrayList<>());
                configList.add(entity.getKey());
            } else {
                String currentConfigGroup = LabelGroupUtils.createLabelGroup(entity.getLabels());
                if (currentConfigGroup.contains(covertGroup)) {
                    List<String> configList = result.computeIfAbsent(
                            LabelGroupUtils.createLabelGroup(entity.getLabels()), configKey -> new ArrayList<>());
                    configList.add(entity.getKey());
                }
            }
        }
        return result;
    }

    private KieResponse getKieResponse(String key, String group, boolean exactMatchFlag) {
        String labelCondition = LabelGroupUtils.getLabelCondition(group);
        final KieRequest cloneRequest = new KieRequest().setRevision(null).setLabelCondition(labelCondition)
                .setKey(key);
        cloneRequest.setAccurateMatchLabel(exactMatchFlag);
        return this.queryConfigurations(cloneRequest);
    }

    @Override
    public boolean removeConfig(String key, String group) {
        Optional<String> keyIdOptional = getKeyId(key, group);
        if (!keyIdOptional.isPresent()) {
            LOGGER.warn("The configuration item does not exist, key is {}, group is {}.", key, group);
            return false;
        }
        final HttpResult httpResult = this.httpClient.doDelete(buildKeyIdUrl(keyIdOptional.get()));
        return httpResult.getCode() == HttpStatus.SC_NO_CONTENT;
    }

    @Override
    public boolean isConnect() {
        String requestUrl = clientUrlManager.getUrl() + "/v1/health";
        KieRequest kieRequest = new KieRequest();
        HttpResult httpResult = httpClient.doGet(requestUrl, kieRequest.getRequestConfig());
        return !httpResult.isError();
    }

    /**
     * Get key_id
     *
     * @param key configuration key
     * @param group configuration group
     * @return key_id, return null if not exists
     */
    public Optional<String> getKeyId(String key, String group) {
        final KieResponse kieResponse = this.getKieResponse(key, group, true);
        if (kieResponse == null || kieResponse.getData() == null) {
            return Optional.empty();
        }
        for (KieConfigEntity entity : kieResponse.getData()) {
            return Optional.of(entity.getId());
        }
        return Optional.empty();
    }
}
