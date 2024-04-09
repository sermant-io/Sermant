/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.utils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;
import com.huaweicloud.sermant.router.spring.cache.AppCache;

import java.util.Locale;
import java.util.Map;

/**
 * Reflection tool class
 *
 * @author provenceee
 * @since 2022-07-19
 */
public class SpringRouterUtils {
    private static final String VERSION_KEY = "version";

    private static final String ZONE_KEY = "zone";

    private SpringRouterUtils() {
    }

    /**
     * Get metadata
     *
     * @param obj Object
     * @return Metadata
     */
    public static Map<String, String> getMetadata(Object obj) {
        return (Map<String, String>) ReflectUtils.invokeWithNoneParameter(obj, "getMetadata");
    }

    /**
     * Deposit metadata
     *
     * @param metadata Metadata
     * @param routerConfig Route configuration
     */
    public static void putMetaData(Map<String, String> metadata, RouterConfig routerConfig) {
        if (metadata == null) {
            return;
        }
        metadata.putIfAbsent(VERSION_KEY, routerConfig.getRouterVersion());
        if (StringUtils.isExist(routerConfig.getZone())) {
            metadata.putIfAbsent(ZONE_KEY, routerConfig.getZone());
        }
        Map<String, String> parameters = routerConfig.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            // The request header is changed to lowercase in the HTTP request
            parameters.forEach((key, value) -> metadata.putIfAbsent(key.toLowerCase(Locale.ROOT), value));
        }
        AppCache.INSTANCE.setMetadata(metadata);
    }
}
