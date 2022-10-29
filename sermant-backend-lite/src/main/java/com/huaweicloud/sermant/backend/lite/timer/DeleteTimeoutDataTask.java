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

package com.huaweicloud.sermant.backend.lite.timer;

import com.huaweicloud.sermant.backend.lite.cache.HeartbeatCache;
import com.huaweicloud.sermant.backend.lite.entity.HeartbeatMessage;

import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

/**
 * 清理过期数据
 *
 * @author xuezechao
 * @since 2022-03-14
 */
public class DeleteTimeoutDataTask extends TimerTask {
    private long maxEffectiveTime;

    private long maxCacheTime;

    /**
     * 构造方法
     *
     * @param maxEffectiveTime 最大有效时间
     * @param maxCacheTime 最大缓存时间
     */
    public DeleteTimeoutDataTask(long maxEffectiveTime, long maxCacheTime) {
        this.maxEffectiveTime = maxEffectiveTime;
        this.maxCacheTime = maxCacheTime;
        deleteHeartbeatCache();
    }

    @Override
    public void run() {
        deleteHeartbeatCache();
    }

    private void deleteHeartbeatCache() {
        Map<String, HeartbeatMessage> heartbeatMessages = HeartbeatCache.getHeartbeatMessageMap();
        for (Iterator<Map.Entry<String, HeartbeatMessage>> it = heartbeatMessages.entrySet().iterator();
            it.hasNext();) {
            Map.Entry<String, HeartbeatMessage> heartbeatMessageEntry = it.next();
            long nowTime = System.currentTimeMillis();
            long receiveTime = heartbeatMessageEntry.getValue().getReceiveTime();
            if ((nowTime - receiveTime) > maxCacheTime) {
                it.remove();
            }
            if ((nowTime - receiveTime) > maxEffectiveTime) {
                heartbeatMessageEntry.getValue().setHealth(false);
            }
        }
    }
}
