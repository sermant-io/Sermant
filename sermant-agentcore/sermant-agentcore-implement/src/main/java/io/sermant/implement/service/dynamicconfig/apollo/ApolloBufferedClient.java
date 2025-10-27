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

import com.ctrip.framework.apollo.ConfigChangeListener;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * wraps the Apollo open apis and client to provides easier apis
 *
 * @author Chen Zhenyang
 * @since 2025-08-07
 */
public class ApolloBufferedClient implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(ApolloBufferedClient.class.getName());
    private final ApolloClient apolloClient;

    /**
     * constructor
     */
    public ApolloBufferedClient() {
        apolloClient = new ApolloClient();
    }

    @Override
    public void close() {
        apolloClient.close();
    }

    /**
     * publish config
     *
     * @param key apollo key
     * @param group apollo namespace
     * @param content apollo value
     * @return check if publish success
     */
    public boolean publishConfig(String key, String group, String content) {
        return apolloClient.publishConfig(key, group, content);
    }

    /**
     * remove config
     *
     * @param key apollo key
     * @param group apollo namespace
     * @return check if remove success
     */
    public boolean removeConfig(String key, String group) {
        return apolloClient.removeConfig(key, group);
    }

    /**
     * get key list from group
     *
     * @param group apollo namespace
     * @return List of keys from group
     */
    public List<String> getListKeysFromGroup(String group) {
        Map<String, List<String>> res = apolloClient.getConfigList(null, group, true);
        return res.get(group);
    }

    /**
     * get config
     *
     * @param key apollo key
     * @param group apollo namespace
     * @return value of the key
     */
    public String getConfig(String key, String group) {
        return apolloClient.getConfig(key, group);
    }

    /**
     * add group listener
     *
     * @param group apollo namespace
     * @param apolloListener apollo listener
     * @return check if add group listener success
     */
    public boolean addGroupListener(String group, ConfigChangeListener apolloListener) {
        return apolloClient.addGroupListener(group, apolloListener);
    }

    /**
     * add config listener
     *
     * @param key apollo key
     * @param group apollo namespace
     * @param apolloListener apollo listener
     * @return check if add config listener success
     */
    public boolean addConfigListener(String key, String group, ConfigChangeListener apolloListener) {
        return apolloClient.addConfigListener(key, group, apolloListener);
    }

    /**
     * remove listener
     *
     * @param validGroup apollo namespace
     * @param ccl apollo listener
     * @return check if remove listener success
     */
    public boolean removeConfigListener(String validGroup, ConfigChangeListener ccl) {
        return apolloClient.removeConfigListener(validGroup, ccl);
    }
}
