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

package com.huaweicloud.sermant.router.dubbo.utils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * URL中parameters参数操作
 *
 * @author chengyouling
 * @since 2022-12-29
 */
public class ParametersUtils {
    private ParametersUtils() {
    }

    /**
     * parameters中增加版本号、路由标签
     *
     * @param parameters 参数集合
     * @param routerConfig sermant参数
     * @return 参数集合
     */
    public static Map<String, String> putParameters(Map<String, String> parameters, RouterConfig routerConfig) {
        Map<String, String> map = Optional.ofNullable(parameters).orElseGet(HashMap::new);
        map.put(RouterConstant.VERSION_KEY, routerConfig.getRouterVersion());
        if (StringUtils.isExist(routerConfig.getZone())) {
            map.putIfAbsent(RouterConstant.ZONE_KEY, routerConfig.getZone());
        }
        Map<String, String> metaParameters = routerConfig.getParameters();
        if (!CollectionUtils.isEmpty(metaParameters)) {
            metaParameters.forEach(
                // 请求头在http请求中，会统一转成小写
                (key, value) -> map.put(RouterConstant.PARAMETERS_KEY_PREFIX + key.toLowerCase(Locale.ROOT), value));
        }
        return map;
    }
}
