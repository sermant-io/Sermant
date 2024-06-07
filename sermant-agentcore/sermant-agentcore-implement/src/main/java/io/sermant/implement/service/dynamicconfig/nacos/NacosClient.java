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

package io.sermant.implement.service.dynamicconfig.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.auth.impl.process.HttpLoginProcessor;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientManager;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;

import io.sermant.implement.service.dynamicconfig.ConfigClient;
import io.sermant.implement.service.dynamicconfig.common.DynamicConstants;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * {@link ConfigService} wrapper, which wraps the Nacos native apis and provides easier apis to use
 *
 * @author zhp
 * @since 2024-05-17
 */
public class NacosClient implements ConfigClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosClient.class.getName());
    /**
     * HTTP protocol
     */
    private static final String HTTP_PROTOCOL = "http://";

    private static final String KEY_ACCESS_TOKEN = "accessToken";

    private static final String KEY_TOKEN_TTL = "tokenTtl";

    private static final String KEY_DATA_ID = "dataId";

    private static final String KEY_PAGE_ITEMS = "pageItems";

    private static final String KEY_GROUP = "group";

    private static final long DEFAULT_REQUEST_TIMEOUT = 6000L;

    /**
     * Nacos refresh window for http request security authentication token
     */
    private static final long TOKEN_REFRESH_WINDOW = 3000L;

    private static final String URL = "/nacos/v1/cs/configs?appName=&config_tags=&pageNo=1&pageSize=";

    private final Properties properties;

    private final ConfigService configService;

    /**
     * Last refresh time of http request security authentication token of Nacos
     */
    private long lastRefreshTime;

    /**
     * Ttl of http request security authentication token of Nacos
     */
    private long tokenTtl;

    /**
     * Http request security authentication token of Nacos
     */
    private String lastToken;

    /**
     * Create a NacosClient and initialize
     *
     * @param properties configuration information for Nacos connection
     * @throws NacosException An exception occurred while creating the Nacos client
     */
    public NacosClient(Properties properties) throws NacosException {
        this.properties = properties;
        configService = NacosFactory.createConfigService(properties);
    }

    @Override
    public String getConfig(String key, String group) {
        try {
            return this.getConfig(key, group, DEFAULT_REQUEST_TIMEOUT);
        } catch (NacosException e) {
            LOGGER.error("Exception in querying configuration.", e);
            return DynamicConstants.EMPTY_STRING;
        }
    }

    /**
     * Get configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @param timeout Request timeout
     * @return configuration content
     * @throws NacosException Configuration query exception
     */
    public String getConfig(String key, String group, long timeout) throws NacosException {
        final String data = this.configService.getConfig(key, group, timeout);
        return data == null ? DynamicConstants.EMPTY_STRING : data;
    }

    @Override
    public Map<String, List<String>> getConfigList(String key, String group, boolean flag) {
        try {
            return getGroupKeys(key, group, properties.getProperty(PropertyKeyConst.NAMESPACE), flag);
        } catch (IOException e) {
            LOGGER.error("Exception in querying configuration list.", e);
            return new HashMap<>();
        }
    }

    /**
     * Publish configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @param content configuration content
     * @return publish result
     */
    public boolean publishConfig(String key, String group, String content) {
        try {
            return this.configService.publishConfig(key, group, content);
        } catch (NacosException e) {
            LOGGER.error("Exception in publishing configuration.", e);
            return false;
        }
    }

    /**
     * Delete configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return remove result
     */
    public boolean removeConfig(String key, String group) {
        try {
            return this.configService.removeConfig(key, group);
        } catch (NacosException e) {
            LOGGER.error("Exception in removing configuration.", e);
            return false;
        }
    }

    @Override
    public boolean isConnect() {
        return "UP".equals(this.configService.getServerStatus());
    }

    /**
     * Add listener
     *
     * @param key configuration key
     * @param group configuration group
     * @param listener listener
     * @return add result
     * @throws NacosException An exception occurred when adding a listener
     */
    public boolean addListener(String key, String group, Listener listener) throws NacosException {
        this.configService.addListener(key, group, listener);
        return true;
    }

    /**
     * Remove listener
     *
     * @param key configuration key
     * @param group configuration group
     * @param listener listener
     */
    public void removeListener(String key, String group, Listener listener) {
        this.configService.removeListener(key, group, listener);
    }

    /**
     * Close service
     *
     * @throws NacosException An exception occurred when closing the service
     */
    public void close() throws NacosException {
        configService.shutDown();
    }

    /**
     * get server status
     *
     * @return server status
     */
    public String getServerStatus() {
        return configService.getServerStatus();
    }

    /**
     * Get all keys for all Nacos groups
     *
     * @param key configuration key
     * @param group configuration group
     * @param namespace configuration namespace
     * @param exactMatchFlag Identification of exact match
     * @return A Map of the groups and all its keys
     * @throws IOException IO exception during service invocation process
     */
    public Map<String, List<String>> getGroupKeys(String key, String group, String namespace, boolean exactMatchFlag)
            throws IOException {
        final String httpResult = doRequest(buildUrl(key, group, namespace, exactMatchFlag));
        if ("".equals(httpResult)) {
            return new HashMap<>();
        }
        Map<String, List<String>> groupKeys = new HashMap<>();
        JSONObject jsonObject = JSONObject.parseObject(httpResult);
        JSONArray pageItems = jsonObject.getJSONArray(KEY_PAGE_ITEMS);

        for (int i = 0; i < pageItems.size(); i++) {
            JSONObject itemObject = pageItems.getJSONObject(i);
            String dataId = itemObject.getString(KEY_DATA_ID);
            String dataGroup = itemObject.getString(KEY_GROUP);
            List<String> dataIdList = groupKeys.getOrDefault(dataGroup, new ArrayList<>());
            dataIdList.add(dataId);
            groupKeys.put(dataGroup, dataIdList);
        }
        return groupKeys;
    }

    /**
     * Build urls for Nacos http request to query all groups and keys
     *
     * @param key configuration key
     * @param group configuration group
     * @param namespace configuration namespace
     * @param exactMatchFlag Identification of exact match
     * @return url
     */
    private String buildUrl(String key, String group, String namespace, boolean exactMatchFlag) {
        final StringBuilder requestUrl = new StringBuilder().append(HTTP_PROTOCOL);
        int pageSize = Integer.MAX_VALUE;
        if (exactMatchFlag) {
            requestUrl.append(properties.getProperty(PropertyKeyConst.SERVER_ADDR)).append(URL).append(pageSize)
                    .append("&dataId=").append(key == null ? DynamicConstants.EMPTY_STRING : key)
                    .append("&group=").append(group == null ? DynamicConstants.EMPTY_STRING : group)
                    .append("&tenant=").append(namespace).append("&search=accurate");
        } else {
            requestUrl.append(properties.getProperty(PropertyKeyConst.SERVER_ADDR)).append(URL).append(pageSize)
                    .append("&dataId=*").append(key == null ? DynamicConstants.EMPTY_STRING : key)
                    .append("*&group=*").append(group == null ? DynamicConstants.EMPTY_STRING : group)
                    .append("*&tenant=").append(namespace).append("&search=blur");
        }
        if (properties.get(PropertyKeyConst.USERNAME) != null && properties.get(PropertyKeyConst.PASSWORD) != null) {
            String accessToken = getToken();
            requestUrl.append("&accessToken=").append(accessToken).append("&username=")
                    .append(properties.get(PropertyKeyConst.USERNAME));
        }
        return requestUrl.toString();
    }

    /**
     * Get token
     *
     * @return token
     */
    private String getToken() {
        if ((System.currentTimeMillis() - lastRefreshTime) >= TimeUnit.SECONDS
                .toMillis(tokenTtl - TOKEN_REFRESH_WINDOW)) {
            HttpLoginProcessor httpLoginProcessor = new HttpLoginProcessor(
                    NamingHttpClientManager.getInstance().getNacosRestTemplate());
            LoginIdentityContext loginIdentityContext = httpLoginProcessor.getResponse(properties);
            lastToken = loginIdentityContext.getParameter(KEY_ACCESS_TOKEN);
            tokenTtl = Long.parseLong(loginIdentityContext.getParameter(KEY_TOKEN_TTL));
            lastRefreshTime = System.currentTimeMillis();
        }
        return lastToken;
    }

    /**
     * HTTP get request
     *
     * @param url HTTP request url
     * @return response body
     * @throws IOException IO exception during service invocation process
     */
    private String doRequest(String url) throws IOException {
        String result;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            int timeOut = Integer.parseInt(properties.getProperty(PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT));
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeOut) // Timeout for connecting to the host
                    .setConnectionRequestTimeout(timeOut) // Request timeout
                    .setSocketTimeout(timeOut) // Read timeout
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);
            }
        }
        return result;
    }
}