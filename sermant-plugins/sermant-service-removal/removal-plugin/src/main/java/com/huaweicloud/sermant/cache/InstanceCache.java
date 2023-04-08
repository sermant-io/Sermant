/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.cache;

import com.huaweicloud.sermant.config.RemovalRule;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.entity.InstanceInfo;
import com.huaweicloud.sermant.entity.RequestInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 服务实例缓存类
 *
 * @author zhp
 * @since 2023-02-17
 */
public class InstanceCache {
    /**
     * 实例信息缓存MAP
     */
    public static final Map<String, InstanceInfo> INSTANCE_MAP = new ConcurrentHashMap<>();

    private static final String CONNECTOR = ":";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private InstanceCache() {
    }

    /**
     * 调用信息保存
     *
     * @param requestInfo 服务调用信息
     */
    public static void saveInstanceInfo(RequestInfo requestInfo) {
        String key = requestInfo.getHost() + CONNECTOR + requestInfo.getPort();
        InstanceInfo info = INSTANCE_MAP.computeIfAbsent(key, s -> {
            InstanceInfo instanceInfo = new InstanceInfo();
            instanceInfo.setHost(requestInfo.getHost());
            instanceInfo.setPort(requestInfo.getPort());
            return instanceInfo;
        });
        if (!requestInfo.isResult()) {
            info.getRequestFailNum().getAndIncrement();
        }
        info.getRequestNum().getAndIncrement();
        info.setLastInvokeTime(requestInfo.getRequestTime());
    }

    /**
     * 判断是否需要摘除实例
     *
     * @param info 实例信息
     * @param rule 离群实例摘除规则
     * @return 是否需要摘除实例
     */
    public static boolean isNeedRemoval(InstanceInfo info, RemovalRule rule) {
        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger requestFailCount = new AtomicInteger();
        if (info.getCountDataList() == null || info.getCountDataList().isEmpty()) {
            return false;
        }
        info.getCountDataList().forEach(requestCountData -> {
            requestCount.getAndAdd(requestCountData.getRequestNum());
            requestFailCount.getAndAdd(requestCountData.getRequestFailNum());
        });
        float errorRate = 0.0f;
        if (requestCount.get() != 0) {
            errorRate = (float) requestFailCount.get() / requestCount.get();
        }
        return errorRate >= rule.getErrorRate();
    }

    /**
     * 摘除实例恢复
     */
    public static void recovery() {
        if (INSTANCE_MAP.isEmpty()) {
            return;
        }
        for (Map.Entry<String, InstanceInfo> entry : INSTANCE_MAP.entrySet()) {
            InstanceInfo info = entry.getValue();
            if (info != null && info.getRemovalStatus().get() && System.currentTimeMillis() > info.getRecoveryTime()
                    && info.getRemovalStatus().compareAndSet(true, false)) {
                LOGGER.info("The removal strength has reached the recovery time, and the removal is canceled");
            }
        }
    }
}
