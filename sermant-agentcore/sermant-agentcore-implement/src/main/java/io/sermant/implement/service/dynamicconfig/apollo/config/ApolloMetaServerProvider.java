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

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.internals.DefaultMetaServerProvider;

import io.sermant.core.utils.StringUtils;

/**
 * customized meta server provider of apollo
 *
 * @author Chen Zhenyang
 * @since 2025-08-17
 */
public class ApolloMetaServerProvider extends DefaultMetaServerProvider {

    @Override
    public String getMetaServerAddress(Env env) {
        String val = ApolloProperty.getUrl();
        return StringUtils.isNoneBlank(val) ? val : super.getMetaServerAddress(env);
    }

    /**
     * priority is higher than DefaultMetaServerProvider but not the highest.
     *
     * @return priority of provider
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
