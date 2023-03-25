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

package com.huaweicloud.sermant.core.event.collector;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.Event;
import com.huaweicloud.sermant.core.event.EventCollector;
import com.huaweicloud.sermant.core.event.EventLevel;
import com.huaweicloud.sermant.core.event.EventType;
import com.huaweicloud.sermant.core.event.LogInfo;
import com.huaweicloud.sermant.core.event.config.EventConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;

/**
 * 日志事件采集器
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public class LogEventCollector extends EventCollector {
    private static LogEventCollector logEventCollector;

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private final ConcurrentHashMap<LogInfo, Long> logInfoOfferTimeCache = new ConcurrentHashMap<>();

    private LogEventCollector() {
    }

    /**
     * 获取日志事件采集器单例
     *
     * @return 日志事件采集器单例
     */
    public static synchronized LogEventCollector getInstance() {
        if (logEventCollector == null) {
            logEventCollector = new LogEventCollector();
        }
        return logEventCollector;
    }

    /**
     * 上报警告日志
     *
     * @param record 日志记录
     */
    public void offerWarning(LogRecord record) {
        if (!eventConfig.isEnable() || !eventConfig.isOfferWarnLog()) {
            return;
        }
        LogInfo logInfo = new LogInfo(record);
        if (checkLogInfoOfferInterval(logInfo)) {
            logInfoOfferTimeCache.put(logInfo, System.currentTimeMillis());
            offerEvent(new Event(EventLevel.IMPORTANT, EventType.LOG, logInfo));
        }
    }

    /**
     * 上报错误日志
     *
     * @param record 日志记录
     */
    public void offerError(LogRecord record) {
        if (!eventConfig.isEnable() || !eventConfig.isOfferErrorLog()) {
            return;
        }
        LogInfo logInfo = new LogInfo(record);
        if (checkLogInfoOfferInterval(logInfo)) {
            logInfoOfferTimeCache.put(logInfo, System.currentTimeMillis());
            offerEvent(new Event(EventLevel.EMERGENCY, EventType.LOG, logInfo));
        }
    }

    /**
     * 通过上报时间间隔，检查是否可以再次上报该日志
     *
     * @param logInfo 事件信息
     * @return boolean 是否可以再次上报
     */
    private boolean checkLogInfoOfferInterval(LogInfo logInfo) {
        Long lastOfferTime = logInfoOfferTimeCache.get(logInfo);
        if (lastOfferTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastOfferTime > eventConfig.getOfferInterval();
    }

    /**
     * 定时清理日志的上报时间缓存
     */
    @Override
    protected void cleanOfferTimeCacheMap() {
        long currentTime = System.currentTimeMillis();
        for (LogInfo logInfo : logInfoOfferTimeCache.keySet()) {
            if (currentTime - logInfoOfferTimeCache.get(logInfo) > eventConfig.getOfferInterval()) {
                logInfoOfferTimeCache.remove(logInfo);
            }
        }
    }
}