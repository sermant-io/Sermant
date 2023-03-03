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

package com.huawei.sermant.backend.webhook;

import com.huawei.sermant.backend.entity.EventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 事件推送
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class EventPushHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPushHandler.class);

    private List<WebHookClient> webHookClients = new ArrayList<>();

    public EventPushHandler() {
        init();
    }

    /**
     * 初始化webhook
     */
    public void init() {
        ServiceLoader<WebHookClient> loader = ServiceLoader.load(WebHookClient.class);
        for (WebHookClient webHookClient : loader) {
            webHookClients.add(webHookClient);
        }
    }

    /**
     * 推送事件
     *
     * @param eventEntities 事件信息
     */
    public void pushEvent(List<EventEntity> eventEntities) {
        if (eventEntities.size() <= 0) {
            LOGGER.error("push event failed, the size of event message is: 0");
            return;
        }
        for (WebHookClient webHookClient : webHookClients) {
            if (webHookClient.getConfig().getEnable()) {
                if (!webHookClient.doNotify(eventEntities)) {
                    // 推送失败
                    LOGGER.error(String.format("push event to webhook:{%s} failed!", webHookClient.getConfig().getName()));
                }
            }
        }
    }

    /**
     * 获取webhook客户端
     *
     * @return webhook客户端
     */
    public List<WebHookClient> getWebHookClients() {
        return webHookClients;
    }
}
