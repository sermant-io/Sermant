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

package com.huawei.discovery.service.lb.utils;

import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.stats.ServiceStatsManager;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 公共测试工具类
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class CommonUtils {
    private CommonUtils(){
    }

    /**
     * 构建实例
     *
     * @param service 服务名
     * @param port 端口
     * @return ServiceInstance
     */
    public static ServiceInstance buildInstance(String service, int port) {
        return new DefaultServiceInstance("localhost", "127.0.0.1", port, Collections.emptyMap(), service);
    }

    /**
     * 清理服务统计数据
     */
    public static void cleanServiceStats() {
        final Optional<Object> serverStatsCache = ReflectUtils
                .getFieldValue(ServiceStatsManager.INSTANCE, "serverStatsCache");
        if (serverStatsCache.isPresent() && serverStatsCache.get() instanceof Map) {
            ((Map<?, ?>) serverStatsCache.get()).clear();
        }
    }
}
