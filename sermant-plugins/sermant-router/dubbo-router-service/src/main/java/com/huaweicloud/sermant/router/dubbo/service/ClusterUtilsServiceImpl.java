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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ClusterUtils的service
 *
 * @author provenceee
 * @since 2022-03-09
 */
public class ClusterUtilsServiceImpl implements ClusterUtilsService {
    private static final String APPLICATION_KEY = "application";

    private static final int EXPECT_LENGTH = 2;

    /**
     * 从url中缓存接口与下游服务名的映射关系，从map中删除标签相关的参数
     *
     * @param arguments 请求参数
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    @Override
    public void doBefore(Object[] arguments) {
        if (arguments.length < EXPECT_LENGTH) {
            return;
        }

        // 保存接口与服务名之间的映射
        DubboCache.INSTANCE.putApplication(DubboReflectUtils.getServiceInterface(arguments[0]),
                DubboReflectUtils.getParameter(arguments[0], APPLICATION_KEY));
        if (arguments[1] instanceof Map<?, ?>) {
            // 本地参数的map，需要把这个map中的标签删除，才能让下游invoker的标签不被本地参数覆盖，即保留下游invoker的标签
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