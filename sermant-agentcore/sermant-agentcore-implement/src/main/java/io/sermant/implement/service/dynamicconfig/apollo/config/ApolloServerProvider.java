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

package io.sermant.implement.service.dynamicconfig.apollo.config;

import com.ctrip.framework.foundation.internals.provider.DefaultServerProvider;

import io.sermant.core.utils.StringUtils;

/**
 * customized server provider of apollo
 *
 * @author Chen Zhenyang
 * @since 2025-08-14
 */
public class ApolloServerProvider extends DefaultServerProvider {
    private static final String APOLLO_CLUSTER = "apollo.cluster";

    @Override
    public String getEnvType() {
        String envType = ApolloProperty.getEnv();
        return envType != null ? envType : super.getEnvType();
    }

    @Override
    public boolean isEnvTypeSet() {
        return StringUtils.isNoneBlank(ApolloProperty.getEnv()) || super.isEnvTypeSet();
    }

    /**
     * get meta property from server provider
     *
     * @param name key of property
     * @param defaultValue default value
     * @return value of property
     */
    @Override
    public String getProperty(String name, String defaultValue) {
        if (APOLLO_CLUSTER.equals(name)) {
            String cluster = ApolloProperty.getCluster();
            if (StringUtils.isNoneBlank(cluster)) {
                return cluster;
            }
        }
        return super.getProperty(name, defaultValue);
    }
}
