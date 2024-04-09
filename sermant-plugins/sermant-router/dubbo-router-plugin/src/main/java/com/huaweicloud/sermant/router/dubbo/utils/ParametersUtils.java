/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.utils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Parameters parameter operation in URL
 *
 * @author chengyouling
 * @since 2022-12-29
 */
public class ParametersUtils {
    private ParametersUtils() {
    }

    /**
     * Add version number and routing label to parameters
     *
     * @param parameters participation set
     * @param routerConfig Sermant parameter
     * @return Parameter set
     */
    public static Map<String, String> putParameters(Map<String, String> parameters, RouterConfig routerConfig) {
        Map<String, String> map = Optional.ofNullable(parameters).orElseGet(HashMap::new);
        Map<String, String> cacheMap = new HashMap<>();
        map.put(RouterConstant.META_VERSION_KEY, routerConfig.getRouterVersion());
        cacheMap.put(RouterConstant.META_VERSION_KEY, routerConfig.getRouterVersion());
        if (StringUtils.isExist(routerConfig.getZone())) {
            map.putIfAbsent(RouterConstant.META_ZONE_KEY, routerConfig.getZone());
            cacheMap.put(RouterConstant.META_ZONE_KEY, map.get(RouterConstant.META_ZONE_KEY));
        }
        Map<String, String> metaParameters = routerConfig.getParameters();
        if (!CollectionUtils.isEmpty(metaParameters)) {
            metaParameters.forEach(
                    (key, value) -> {
                        // The request header will be uniformly converted to lowercase in HTTP requests
                        String lowerCaseKey = key.toLowerCase(Locale.ROOT);

                        // Replacing "-" with "." is for traffic routing compatibility between 2.5.0 and 2.5.6
                        map.put(RouterConstant.PARAMETERS_KEY_PREFIX + lowerCaseKey
                                .replace(RouterConstant.DASH, RouterConstant.POINT), value);
                        cacheMap.put(RouterConstant.PARAMETERS_KEY_PREFIX + lowerCaseKey, value);
                    });
        }
        DubboCache.INSTANCE.setParameters(cacheMap);
        return map;
    }
}
