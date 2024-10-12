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

package io.sermant.core.event.collector;

import io.sermant.core.event.Event;
import io.sermant.core.event.EventCollector;
import io.sermant.core.event.EventLevel;
import io.sermant.core.event.EventType;
import io.sermant.core.event.LogInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;

/**
 * Log event collector
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public class LogEventCollector extends EventCollector {
    private static LogEventCollector logEventCollector;

    private final ConcurrentHashMap<LogInfo, Long> logInfoOfferTimeCache = new ConcurrentHashMap<>();

    private LogEventCollector() {
    }

    /**
     * Obtain the singleton of the log event collector
     *
     * @return singleton
     */
    public static synchronized LogEventCollector getInstance() {
        if (logEventCollector == null) {
            logEventCollector = new LogEventCollector();
        }
        return logEventCollector;
    }

    /**
     * Report warning log
     *
     * @param record log record
     */
    public void offerWarning(LogRecord record) {
        if (!isEnableEvent() || !eventConfig.isOfferWarnLog()) {
            return;
        }
        LogInfo logInfo = new LogInfo(record);
        if (checkLogInfoOfferInterval(logInfo)) {
            logInfoOfferTimeCache.put(logInfo, System.currentTimeMillis());
            offerEvent(new Event(EventLevel.IMPORTANT, EventType.LOG, logInfo));
        }
    }

    /**
     * Report error log
     *
     * @param record log record
     */
    public void offerError(LogRecord record) {
        if (!isEnableEvent() || !eventConfig.isOfferErrorLog()) {
            return;
        }
        LogInfo logInfo = new LogInfo(record);
        if (checkLogInfoOfferInterval(logInfo)) {
            logInfoOfferTimeCache.put(logInfo, System.currentTimeMillis());
            offerEvent(new Event(EventLevel.EMERGENCY, EventType.LOG, logInfo));
        }
    }

    /**
     * Check whether the log can be reported again based on the reporting interval
     *
     * @param logInfo log information
     * @return boolean Whether it can be reported again
     */
    private boolean checkLogInfoOfferInterval(LogInfo logInfo) {
        Long lastOfferTime = logInfoOfferTimeCache.get(logInfo);
        if (lastOfferTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastOfferTime > eventConfig.getOfferInterval();
    }

    /**
     * Periodically clear the log reporting time cache
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
