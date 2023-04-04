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

package com.huaweicloud.sermant.core.event.config;

import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;

/**
 * 事件配置
 *
 * @author luanwenfei
 * @since 2023-03-08
 */
@ConfigTypeKey("event")
public class EventConfig implements BaseConfig {
    /**
     * 默认事件发送时间间隔
     */
    private static final long DEFAULT_SEND_INTERVAL = 30000L;

    /**
     * 默认事件记录间隔
     */
    private static final long DEFAULT_OFFER_INTERVAL = 60000L;

    /**
     * 事件开关
     */
    private boolean enable = false;

    /**
     * 上报Error日志开关
     */
    private boolean offerErrorLog = false;

    /**
     * 上报Warn日志开关
     */
    private boolean offerWarnLog = false;

    /**
     * 事件发送时间间隔(ms)
     */
    private long sendInterval = DEFAULT_SEND_INTERVAL;

    /**
     * 事件记录时间间隔(ms),在一定时间内重复事件压缩
     */
    private long offerInterval = DEFAULT_OFFER_INTERVAL;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isOfferErrorLog() {
        return offerErrorLog;
    }

    public void setOfferErrorLog(boolean offerErrorLog) {
        this.offerErrorLog = offerErrorLog;
    }

    public boolean isOfferWarnLog() {
        return offerWarnLog;
    }

    public void setOfferWarnLog(boolean offerWarnLog) {
        this.offerWarnLog = offerWarnLog;
    }

    public long getSendInterval() {
        return sendInterval;
    }

    public void setSendInterval(long sendInterval) {
        this.sendInterval = sendInterval;
    }

    public long getOfferInterval() {
        return offerInterval;
    }

    public void setOfferInterval(long offerInterval) {
        this.offerInterval = offerInterval;
    }
}
