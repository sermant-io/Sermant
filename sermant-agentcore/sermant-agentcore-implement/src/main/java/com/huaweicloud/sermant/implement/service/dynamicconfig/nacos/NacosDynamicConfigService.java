/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.dynamicconfig.nacos;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.AesUtil;
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.client.auth.impl.process.HttpLoginProcessor;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientManager;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dynamic configuration service, Nacos implementation
 *
 * @author tangle
 * @since 2023-08-17
 */
public class NacosDynamicConfigService extends DynamicConfigService {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * HTTP protocol
     */
    private static final String HTTP_PROTOCOL = "http://";

    private static final String KEY_ACCESS_TOKEN = "accessToken";

    private static final String KEY_TOKEN_TTL = "tokenTtl";

    private static final String KEY_DATA_ID = "dataId";

    private static final String KEY_PAGE_ITEMS = "pageItems";

    private static final String KEY_GROUP = "group";

    private static final String KEY_SERVER = "server";

    /**
     * NacosListener type
     */
    private static final String TYPE_GROUP = "GROUP";

    private static final String TYPE_KEY = "KEY";

    /**
     * Update interval for listener
     */
    private static final long UPDATE_TIME_INTERVAL = 3000L;

    /**
     * Nacos refresh window for http request security authentication token
     */
    private static final long TOKEN_REFRESH_WINDOW = 3000L;

    /**
     * The thread pool for updating listeners periodically
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private NacosBufferedClient nacosClient;

    private final ServiceMeta serviceMeta;

    private final List<NacosListener> listeners;

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
     * Constructor: Compile the regular expression and initialize the List
     */
    public NacosDynamicConfigService() {
        listeners = new ArrayList<>();
        serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
    }

    @Override
    public void start() {
        if (CONFIG.isEnableAuth()) {
            nacosClient = new NacosBufferedClient(CONFIG.getServerAddress(), CONFIG.getTimeoutValue(),
                    serviceMeta.getProject(), CONFIG.getUserName(), CONFIG.getPassword());
        } else {
            nacosClient = new NacosBufferedClient(CONFIG.getServerAddress(), CONFIG.getTimeoutValue(),
                    serviceMeta.getProject());
        }
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(this::updateConfigListener, UPDATE_TIME_INTERVAL,
                UPDATE_TIME_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (nacosClient != null) {
            nacosClient.close();
        }
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
        listeners.clear();
    }

    @Override
    public Optional<String> doGetConfig(String key, String group) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos get config failed, group name is invalid. group: {1}", group);
            return Optional.empty();
        }
        return Optional.ofNullable(nacosClient.getConfig(key, NacosUtils.reBuildGroup(group)));
    }

    @Override
    public boolean doPublishConfig(String key, String group, String content) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos publish config failed, group name is invalid. group: {1}", group);
            return false;
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        boolean result = nacosClient.publishConfig(key, validGroup, content);
        if (result) {
            LOGGER.log(Level.INFO, "nacos config publish success, key: {0}, group: {1}, content: {2}",
                    new String[]{key, validGroup, content});
        } else {
            LOGGER.log(Level.SEVERE, "nacos config publish failed, key: {0}, group: {1}, content: {2}",
                    new String[]{key, validGroup, content});
        }
        return result;
    }

    @Override
    public boolean doRemoveConfig(String key, String group) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos remove config failed, group name is invalid. group: {1}", group);
            return false;
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        boolean result = nacosClient.removeConfig(key, validGroup);
        if (result) {
            LOGGER.log(Level.INFO, "nacos config remove success, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        } else {
            LOGGER.log(Level.SEVERE, "nacos config remove failed, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        }
        return result;
    }

    @Override
    public boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos add config Listener failed, group name is invalid. group: {1}", group);
            return false;
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        Listener nacosListener = instantiateListener(key, validGroup, listener);
        boolean result = nacosClient.addListener(key, validGroup, nacosListener);
        if (result) {
            Map<String, Listener> keyListener = new HashMap<>();
            keyListener.put(key, nacosListener);
            listeners.add(new NacosListener(TYPE_KEY, validGroup, keyListener, listener));
            LOGGER.log(Level.INFO, "nacos add config listener success, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        } else {
            LOGGER.log(Level.SEVERE, "nacos add config listener failed, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        }
        return result;
    }

    @Override
    public boolean doRemoveConfigListener(String key, String group) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos remove config Listener failed, group name is invalid. group: {1}", group);
            return false;
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        List<NacosListener> listenerList = getListener(key, validGroup, TYPE_KEY);
        for (NacosListener nacosListener : listenerList) {
            nacosClient.removeListener(key, validGroup, nacosListener.getKeyListener().get(key));
            listeners.remove(nacosListener);
        }
        LOGGER.log(Level.INFO, "nacos remove config listener success, key: {0}, group: {1}",
                new String[]{key, validGroup});
        return true;
    }

    @Override
    public boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos add group config Listener failed, group name is invalid. group: {1}",
                    group);
            return false;
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        List<String> keys = doListKeysFromGroup(validGroup);
        Map<String, Listener> keyListenerMap = new HashMap<>();
        for (String key : keys) {
            Listener nacosListener = instantiateListener(key, validGroup, listener);
            if (!nacosClient.addListener(key, validGroup, nacosListener)) {
                LOGGER.log(Level.SEVERE, "nacos add group config listener failed, key: {0}, group: {1}",
                        new String[]{key, validGroup});
                return false;
            }
            keyListenerMap.put(key, nacosListener);
        }
        listeners.add(new NacosListener(TYPE_GROUP, validGroup, keyListenerMap, listener));
        LOGGER.log(Level.INFO, "nacos add group config listener success, group: {0}", validGroup);
        return true;
    }

    @Override
    public boolean doRemoveGroupListener(String group) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos remove group config Listener failed, group name is invalid. group: {1}",
                    group);
            return false;
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        List<NacosListener> listenerList = getGroupListener(validGroup);
        for (NacosListener nacosListener : listenerList) {
            for (Map.Entry<String, Listener> entry : nacosListener.getKeyListener().entrySet()) {
                String key = entry.getKey();
                Listener listener = entry.getValue();
                nacosClient.removeListener(key, validGroup, listener);
            }
            listeners.remove(nacosListener);
        }
        LOGGER.log(Level.INFO, "nacos remove group config listener success, group: {0}", validGroup);
        return true;
    }

    @Override
    public List<String> doListKeysFromGroup(String group) {
        if (!NacosUtils.isValidGroupName(group)) {
            LOGGER.log(Level.SEVERE, "nacos list keys from group failed, group name is invalid. group: {1}",
                    group);
            return Collections.emptyList();
        }
        String validGroup = NacosUtils.reBuildGroup(group);
        List<String> resultList = getGroupKeys().get(validGroup);
        return CollectionUtils.isEmpty(resultList) ? Collections.emptyList() : resultList;
    }

    /**
     * Get listener
     *
     * @param key configuration key
     * @param validGroup valid configuration group
     * @param type listener type
     * @return listener list
     */
    private List<NacosListener> getListener(String key, String validGroup, String type) {
        List<NacosListener> list = new ArrayList<>();
        for (NacosListener nacosListener : listeners) {
            if (!nacosListener.getType().equals(type) || !nacosListener.getGroup().equals(validGroup)
                    || !nacosListener.getKeyListener().containsKey(key)) {
                continue;
            }
            list.add(nacosListener);
        }
        return list;
    }

    /**
     * Instantiate listener
     *
     * @param key configuration key
     * @param validGroup valid configuration group
     * @param listener dynamic configuration listener
     * @return Nacos listener
     */
    private Listener instantiateListener(String key, String validGroup, DynamicConfigListener listener) {
        return new Listener() {
            private final Executor defaultExecutor = Executors.newSingleThreadExecutor();

            private boolean isCreateOrModify = false;

            @Override
            public Executor getExecutor() {
                return defaultExecutor;
            }

            @Override
            public void receiveConfigInfo(String content) {
                listener.process(listenerEvent(key, validGroup, content));
            }

            /**
             * Get listener event
             *
             * @param key configuration key
             * @param validGroup valid configuration group
             * @param content configuration content
             * @return DynamicConfigEvent
             */
            private DynamicConfigEvent listenerEvent(String key, String validGroup, String content) {
                if (content == null) {
                    // remove
                    isCreateOrModify = false;
                    return DynamicConfigEvent.deleteEvent(key, validGroup, null);
                } else if (isCreateOrModify) {
                    // modify
                    return DynamicConfigEvent.modifyEvent(key, validGroup, content);
                } else {
                    // create
                    isCreateOrModify = true;
                    return DynamicConfigEvent.createEvent(key, validGroup, content);
                }
            }
        };
    }

    /**
     * Get all listeners in the group
     *
     * @param validGroup valid configuration group
     * @return Nacos listener list
     */
    private List<NacosListener> getGroupListener(String validGroup) {
        List<NacosListener> list = new ArrayList<>();
        for (NacosListener nacosListener : listeners) {
            if (!nacosListener.getType().equals(TYPE_GROUP) || !nacosListener.getGroup().equals(validGroup)) {
                continue;
            }
            list.add(nacosListener);
        }
        return list;
    }

    /**
     * Update group listeners periodically
     */
    private void updateConfigListener() {
        Map<String, List<String>> groupKeys = getGroupKeys();
        for (NacosListener nacosListener : listeners) {
            if (!nacosListener.getType().equals(TYPE_GROUP)) {
                continue;
            }
            String group = nacosListener.getGroup();
            List<String> truthKeys = groupKeys.getOrDefault(group, Collections.emptyList());
            if (CollectionUtils.isEmpty(truthKeys)) {
                continue;
            }
            Map<String, Listener> listenerMap = nacosListener.getKeyListener();

            // Iterate all keys in Nacos group
            for (String key : truthKeys) {
                if (doUpdateConfigListener(nacosListener, group, listenerMap, key)) {
                    break;
                }
            }
        }
    }

    private boolean doUpdateConfigListener(NacosListener nacosListener, String group, Map<String, Listener> listenerMap,
            String key) {
        Listener listenerNacos;
        if (!listenerMap.containsKey(key)) {
            listenerNacos = instantiateListener(key, group, nacosListener.getDynamicConfigListener());
            boolean result = nacosClient.addListener(key, group, listenerNacos);
            if (!result) {
                LOGGER.log(Level.SEVERE, "Nacos add listener failed group is {0} and key is {1}. ",
                        new String[]{group, key});
                return true;
            }
            listenerMap.put(key, listenerNacos);
        }
        return false;
    }

    /**
     * Get all keys for all Macos groups
     *
     * @return A Map of the groups and all its keys
     */
    private Map<String, List<String>> getGroupKeys() {
        final String httpResult = doRequest(buildUrl());
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
     * @return url
     */
    private String buildUrl() {
        final StringBuilder requestUrl = new StringBuilder().append(HTTP_PROTOCOL);
        int pageSize = Integer.MAX_VALUE;
        requestUrl.append(CONFIG.getServerAddress())
                .append("/nacos/v1/cs/configs?dataId=&group=&appName=&config_tags=&pageNo=1&pageSize=")
                .append(pageSize)
                .append("&tenant=")
                .append(serviceMeta.getProject())
                .append("&search=accurate");
        if (CONFIG.isEnableAuth()) {
            String accessToken = getToken();
            requestUrl.append("&accessToken=")
                    .append(accessToken)
                    .append("&username=")
                    .append(AesUtil.decrypt(CONFIG.getPrivateKey(), CONFIG.getUserName()).orElse(""));
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
            LoginIdentityContext loginIdentityContext = httpLoginProcessor.getResponse(getProperties());
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
     */
    private String doRequest(String url) {
        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONFIG.getTimeoutValue()) // Timeout for connecting to the host
                    .setConnectionRequestTimeout(CONFIG.getTimeoutValue()) // Request timeout
                    .setSocketTimeout(CONFIG.getTimeoutValue()) // Read timeout
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Nacos http request exception.");
        }
        return result;
    }

    /**
     * Properties required when building a token
     *
     * @return Properties
     */
    private Properties getProperties() {
        Properties properties = new Properties();
        if (StringUtils.isEmpty(CONFIG.getUserName()) || StringUtils.isEmpty(CONFIG.getPassword())
                || StringUtils.isEmpty(CONFIG.getPrivateKey())) {
            LOGGER.log(Level.SEVERE, "Nacos username, password or privateKey is Empty");
            return properties;
        }
        Optional<String> userName = AesUtil.decrypt(CONFIG.getPrivateKey(), CONFIG.getUserName());
        Optional<String> passWord = AesUtil.decrypt(CONFIG.getPrivateKey(), CONFIG.getPassword());
        if (!userName.isPresent() || !passWord.isPresent()) {
            LOGGER.log(Level.SEVERE, "Nacos username and password parsing failed");
            return properties;
        }
        properties.setProperty(KEY_SERVER, CONFIG.getServerAddress());
        properties.setProperty(PropertyKeyConst.USERNAME, userName.get());
        properties.setProperty(PropertyKeyConst.PASSWORD, passWord.get());
        return properties;
    }
}
