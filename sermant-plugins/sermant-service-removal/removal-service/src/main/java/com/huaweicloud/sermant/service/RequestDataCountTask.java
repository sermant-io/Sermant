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

package com.huaweicloud.sermant.service;

import com.huaweicloud.sermant.cache.InstanceCache;
import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.service.ServiceManager;
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
 * 实例调用信息统计任务
 *
 * @author zhp
 * @since 2023-02-28
 */
public class RequestDataCountTask implements PluginService {
    private static final int SCALE = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final RemovalConfig removalConfig = ConfigManager.getConfig(RemovalConfig.class);

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private RemovalEventService removalEventService;

    /**
     * 数据处理
     */
    private void processData() {
        Map<String, InstanceInfo> instanceInfoMap = InstanceCache.INSTANCE_MAP;
        if (instanceInfoMap.isEmpty()) {
            LOGGER.info("Instance information is empty");
            return;
        }
        for (Iterator<Map.Entry<String, InstanceInfo>> iterator = instanceInfoMap.entrySet().iterator();
                iterator.hasNext(); ) {
            InstanceInfo info = iterator.next().getValue();
            if (System.currentTimeMillis() - info.getLastInvokeTime() >= removalConfig.getExpireTimes()) {
                iterator.remove();
                if (info.getRemovalStatus().get()) {
                    removalEventService.reportRemovalEvent(info);
                }
                LOGGER.info("Instance information expires, remove instance information");
                continue;
            }
            saveRequestCountData(info);
            LOGGER.log(Level.INFO, "The Instance information is {0}", info);
        }
    }

    /**
     * 统计请求信息并缓存
     *
     * @param info 实例信息
     */
    private void saveRequestCountData(InstanceInfo info) {
        RequestCountData countData = new RequestCountData();
        countData.setRequestNum(info.getRequestNum().get());
        countData.setRequestFailNum(info.getRequestFailNum().get());
        calErrorRate(countData);
        if (info.getCountDataList() == null) {
            info.setCountDataList(new ArrayList<>());
        }
        info.getCountDataList().add(countData);
        LOGGER.log(Level.INFO, "The add countData is {0}", countData);
        info.getRequestNum().getAndAdd(-countData.getRequestNum());
        info.getRequestFailNum().getAndAdd(-countData.getRequestFailNum());
        if (info.getCountDataList().size() > removalConfig.getWindowsNum()) {
            info.getCountDataList().remove(0);
        }
    }

    /**
     * 计算错误率
     *
     * @param countData 请求统计信息
     */
    private void calErrorRate(RequestCountData countData) {
        if (countData.getRequestNum() == 0 || countData.getRequestFailNum() == 0) {
            countData.setErrorRate(0);
        } else {
            BigDecimal count = new BigDecimal(countData.getRequestNum());
            BigDecimal failNum = new BigDecimal(countData.getRequestFailNum());
            countData.setErrorRate(failNum.divide(count, SCALE, RoundingMode.HALF_UP).floatValue());
        }
    }

    @Override
    public void start() {
        if (!removalConfig.isEnableRemoval() || removalConfig.getWindowsTimes() == 0
                || removalConfig.getWindowsNum() == 0) {
            return;
        }
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(this::processData, removalConfig.getWindowsTimes(),
                removalConfig.getWindowsTimes(), TimeUnit.MILLISECONDS);
        removalEventService = ServiceManager.getService(RemovalEventService.class);
    }

    @Override
    public void stop() {
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
    }
}
