/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.service;

import com.huaweicloud.sermant.cache.InstanceCache;
import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.entity.InstanceInfo;
import com.huaweicloud.sermant.entity.RequestCountData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The instance invokes the information statistics task
 *
 * @author zhp
 * @since 2023-02-28
 */
public class RequestDataCountTask implements PluginService {
    private static final int SCALE = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final RemovalConfig removalConfig = PluginConfigManager.getPluginConfig(RemovalConfig.class);

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private RemovalEventService removalEventService;

    /**
     * data processing
     */
    private void processData() {
        Map<String, InstanceInfo> instanceInfoMap = InstanceCache.INSTANCE_MAP;
        if (instanceInfoMap.isEmpty()) {
            LOGGER.log(Level.FINE,"Instance information is empty");
            return;
        }
        for (Iterator<Map.Entry<String, InstanceInfo>> iterator = instanceInfoMap.entrySet().iterator();
                iterator.hasNext(); ) {
            InstanceInfo info = iterator.next().getValue();
            if (System.currentTimeMillis() - info.getLastInvokeTime() >= removalConfig.getExpireTime()) {
                iterator.remove();
                if (info.getRemovalStatus().get()) {
                    removalEventService.reportRemovalEvent(info);
                }
                LOGGER.info("Instance information expires, remove instance information");
                continue;
            }
            saveRequestCountData(info);
            LOGGER.log(Level.FINE, "The Instance information is {0}", info);
        }
    }

    /**
     * Collect and cache request information
     *
     * @param info Instance information
     */
    private void saveRequestCountData(InstanceInfo info) {
        RequestCountData countData = new RequestCountData();
        countData.setRequestNum(info.getRequestNum().get());
        countData.setRequestFailNum(info.getRequestFailNum().get());
        if (info.getCountDataList() == null) {
            info.setCountDataList(new ArrayList<>());
        }
        info.getCountDataList().add(countData);
        LOGGER.log(Level.FINE, "The add countData is {0}", countData);
        info.getRequestNum().getAndAdd(-countData.getRequestNum());
        info.getRequestFailNum().getAndAdd(-countData.getRequestFailNum());
        if (info.getCountDataList().size() > removalConfig.getWindowsNum()) {
            info.getCountDataList().remove(0);
        }
        info.setErrorRate(calErrorRate(info));
    }

    /**
     * Calculate the error rate
     *
     * @param info Instance information
     * @return Error rate
     */
    private float calErrorRate(InstanceInfo info) {
        int requestCount = 0;
        int requestFailCount = 0;
        for (RequestCountData requestCountData : info.getCountDataList()) {
            requestCount += requestCountData.getRequestNum();
            requestFailCount += requestCountData.getRequestFailNum();
        }
        if (requestCount == 0 || requestFailCount == 0) {
            return 0;
        } else {
            BigDecimal count = new BigDecimal(requestCount);
            BigDecimal failNum = new BigDecimal(requestFailCount);
            return failNum.divide(count, SCALE, RoundingMode.HALF_UP).floatValue();
        }
    }

    @Override
    public void start() {
        if (removalConfig.getWindowsTime() == 0 || removalConfig.getWindowsNum() == 0) {
            return;
        }
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(this::processData, removalConfig.getWindowsTime(),
                removalConfig.getWindowsTime(), TimeUnit.MILLISECONDS);
        removalEventService = PluginServiceManager.getPluginService(RemovalEventService.class);
    }

    @Override
    public void stop() {
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
    }
}
