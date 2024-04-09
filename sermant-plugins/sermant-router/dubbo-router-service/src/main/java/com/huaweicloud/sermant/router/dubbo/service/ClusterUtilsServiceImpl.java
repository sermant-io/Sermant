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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The service of ClusterUtils
 *
 * @author provenceee
 * @since 2022-03-09
 */
public class ClusterUtilsServiceImpl implements ClusterUtilsService {
    private static final String APPLICATION_KEY = "application";

    private static final int EXPECT_LENGTH = 2;

    /**
     * Cache the mapping between the API and the downstream service name from the URL, and delete the tag-related
     * parameters from the map
     *
     * @param arguments request parameters
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    @Override
    public void doBefore(Object[] arguments) {
        if (arguments.length < EXPECT_LENGTH) {
            return;
        }

        // Saves the mapping between the interface and the service name
        DubboCache.INSTANCE.putApplication(DubboReflectUtils.getServiceInterface(arguments[0]),
                DubboReflectUtils.getParameter(arguments[0], APPLICATION_KEY));
        if (arguments[1] instanceof Map<?, ?>) {
            // For maps with local parameters, you need to delete the tags in this map so that the tags of the
            // downstream invoker are not overwritten by the local parameters,
            // that is, the tags of the downstream invoker are retained
            Map<String, String> localMap = new HashMap<>((Map<String, String>) arguments[1]);
            localMap.entrySet().removeIf(this::isRemove);
            arguments[1] = localMap;
        }
    }

    private boolean isRemove(Entry<String, String> entry) {
        String key = entry.getKey();
        if (key == null) {
            return false;
        }
        if (key.startsWith(RouterConstant.PARAMETERS_KEY_PREFIX)) {
            return true;
        }
        if (RouterConstant.META_VERSION_KEY.equals(key)) {
            return true;
        }
        return RouterConstant.META_ZONE_KEY.equals(key);
    }
}