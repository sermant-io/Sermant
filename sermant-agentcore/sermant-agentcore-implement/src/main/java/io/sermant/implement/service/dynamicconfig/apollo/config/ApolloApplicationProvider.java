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

import com.ctrip.framework.foundation.internals.provider.DefaultApplicationProvider;

import io.sermant.core.utils.StringUtils;

/**
 * customized application provider of apollo
 *
 * @author Chen Zhenyang
 * @since 2025-08-14
 */
public class ApolloApplicationProvider extends DefaultApplicationProvider {
    private static final String APP_ID = "app.id";
    private static final String ACCESS_KEY = "apollo.access-key.secret";

    @Override
    public String getAccessKeySecret() {
        String value = ApolloProperty.getAccessKey();
        return StringUtils.isNoneBlank(value) ? value : super.getAccessKeySecret();
    }

    @Override
    public String getAccessKeySecret(String appId) {
        if (appId.equals(getAppId())) {
            return getAccessKeySecret();
        }
        return super.getAccessKeySecret(appId);
    }

    @Override
    public String getAppId() {
        String value = ApolloProperty.getAppId();
        return StringUtils.isNoneBlank(value) ? value : super.getAppId();
    }

    @Override
    public boolean isAppIdSet() {
        return StringUtils.isNoneBlank(getAppId());
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        String val;
        if (APP_ID.equals(name)) {
            val = getAppId();
            return StringUtils.isNoneBlank(val) ? val : defaultValue;
        } else if (ACCESS_KEY.equals(name)) {
            val = getAccessKeySecret();
            return StringUtils.isNoneBlank(val) ? val : defaultValue;
        } else {
            val = super.getProperty(name, defaultValue);
        }
        return val;
    }
}
