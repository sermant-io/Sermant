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

package com.huaweicloud.sermant.backend.webhook.dingding;

import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.entity.event.DingDingEntity;
import com.huaweicloud.sermant.backend.entity.event.DingDingMessageType;
import com.huaweicloud.sermant.backend.entity.event.EventInfo;
import com.huaweicloud.sermant.backend.entity.event.EventType;
import com.huaweicloud.sermant.backend.entity.event.LogInfo;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.webhook.WebHookClient;
import com.huaweicloud.sermant.backend.webhook.WebHookConfig;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * DingDing webhook client
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class DingDingHookClient implements WebHookClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DingDingHookClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private WebHookConfig dingDingHookConfig = DingDingHookConfig.getInstance();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Constructor
     */
    public DingDingHookClient() {
        dingDingHookConfig.setName(CommonConst.DINGDING_WEBHOOK_NAME);
        dingDingHookConfig.setId(CommonConst.DINGDING_WEBHOOK_ID);
    }

    /**
     * Webhook event notify
     *
     * @param events event information
     * @return true/false
     */
    @Override
    public boolean doNotify(List<QueryResultEventInfoEntity> events) {

        DingDingEntity dingDingEntity = new DingDingEntity();
        dingDingEntity.setMsgtype(DingDingMessageType.MARKDOWN.getType());
        dingDingEntity.setMarkdown(new HashMap<String, String>() {
            {
                put("title", "Sermant Event");
                put("text", getNotifyContent(events));
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        HttpEntity httpEntity = new HttpEntity<>(dingDingEntity, headers);

        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(
                dingDingHookConfig.getUrl(), httpEntity, JSONObject.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("do notify dingding webhook failed, error message:{}", responseEntity.getBody());
            return false;
        }
        return true;
    }

    /**
     * Get configuration
     *
     * @return configuration
     */
    @Override
    public WebHookConfig getConfig() {
        return dingDingHookConfig;
    }

    /**
     * Get notify content
     *
     * @param events events
     * @return notify content
     */
    public String getNotifyContent(List<QueryResultEventInfoEntity> events) {
        StringBuilder result = new StringBuilder();
        for (QueryResultEventInfoEntity event : events) {
            if (event.getType().equals(EventType.LOG.getDescription())) {
                LogInfo logInfo = (LogInfo) event.getInfo();
                result.append(String.format(CommonConst.DINGDING_MARKDOWN_LOG_FORMAT,
                        event.getLevel(),
                        event.getMeta().get("service"),
                        event.getMeta().get("ip"),
                        simpleDateFormat.format(new Date(event.getTime())),
                        event.getScope(),
                        event.getType(),
                        logInfo.getLogLevel(),
                        logInfo.getLogMessage(),
                        logInfo.getLogClass(),
                        logInfo.getLogMethod(),
                        logInfo.getLogLineNumber(),
                        logInfo.getLogThreadId(),
                        logInfo.getLogThrowable()));
            } else {
                EventInfo eventInfo = (EventInfo) event.getInfo();
                result.append(String.format(CommonConst.DINGDING_MARKDOWN_EVENT_FORMAT,
                        event.getLevel(),
                        event.getMeta().get("service"),
                        event.getMeta().get("ip"),
                        simpleDateFormat.format(new Date(event.getTime())),
                        event.getScope(),
                        event.getType(),
                        eventInfo.getName(),
                        eventInfo.getDescription()));
            }
        }
        return result.toString();
    }
}
