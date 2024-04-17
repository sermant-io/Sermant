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

package com.huaweicloud.sermant.backend.webhook.feishu;

import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.entity.event.EventInfo;
import com.huaweicloud.sermant.backend.entity.event.EventType;
import com.huaweicloud.sermant.backend.entity.event.FeiShuContentEntity;
import com.huaweicloud.sermant.backend.entity.event.FeiShuEntity;
import com.huaweicloud.sermant.backend.entity.event.FeiShuMessageType;
import com.huaweicloud.sermant.backend.entity.event.LogInfo;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.webhook.WebHookClient;
import com.huaweicloud.sermant.backend.webhook.WebHookConfig;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * FeiShu webhook client
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class FeiShuHookClient implements WebHookClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeiShuHookClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private WebHookConfig feiShuHookConfig = FeiShuHookConfig.getInstance();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Constructor
     */
    public FeiShuHookClient() {
        feiShuHookConfig.setName(CommonConst.FEISHU_WEBHOOK_NAME);
        feiShuHookConfig.setId(CommonConst.FEISHU_WEBHOOK_ID);
    }

    /**
     * Webhook notify event
     *
     * @param events event information
     * @return notify result
     */
    @Override
    public boolean doNotify(List<QueryResultEventInfoEntity> events) {
        FeiShuEntity feiShuEntity = new FeiShuEntity();
        feiShuEntity.setMsgType(FeiShuMessageType.POST.getType());
        feiShuEntity.setContent(new HashMap<String, Object>() {
            {
                put(FeiShuMessageType.POST.toString(), getNotifyContent(events));
            }
        });

        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(
                feiShuHookConfig.getUrl(), JSONObject.toJSONString(feiShuEntity), JSONObject.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("do notify feishu webhook failed, error message:{}", responseEntity.getBody());
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
        return feiShuHookConfig;
    }

    /**
     * Get notify content
     *
     * @param events event information
     * @return notify content
     */
    public HashMap<String, Object> getNotifyContent(List<QueryResultEventInfoEntity> events) {
        List<List<FeiShuContentEntity>> content = new ArrayList<>();
        for (QueryResultEventInfoEntity event : events) {
            content.add(getContent("level:" + event.getLevel()));
            content.add(getContent("service:" + event.getMeta().get("service")));
            content.add(getContent("ip:" + event.getMeta().get("ip")));
            content.add(getContent("time:" + simpleDateFormat.format(new Date(event.getTime()))));
            content.add(getContent("scope:" + event.getScope()));
            content.add(getContent("type:" + event.getType()));

            if (event.getType().equals(EventType.LOG.getDescription())) {
                LogInfo logInfo = (LogInfo) event.getInfo();
                content.add(getContent("logLevel:" + logInfo.getLogLevel()));
                content.add(getContent("logMessage:" + logInfo.getLogMessage()));
                content.add(getContent("logClass:" + logInfo.getLogClass()));
                content.add(getContent("logMethod:" + logInfo.getLogMethod()));
                content.add(getContent("logLineNumber:" + logInfo.getLogLineNumber()));
                content.add(getContent("logThreadId" + logInfo.getLogThreadId()));
                content.add(getContent("throwable:" + logInfo.getLogThrowable()));
            } else {
                EventInfo eventInfo = (EventInfo) event.getInfo();
                content.add(getContent("name:" + eventInfo.getName()));
                content.add(getContent("description:" + eventInfo.getDescription()));
            }
            content.add(getContent(System.lineSeparator()));
        }
        HashMap<String, Object> contents = new HashMap<>();
        contents.put("title", "Sermant Event");
        contents.put("content", content);
        HashMap<String, Object> result = new HashMap<>();
        result.put("zh_cn", contents);
        return result;
    }

    /**
     * Get rich text of notified event
     *
     * @param text text information
     * @return rich text of notified event
     */
    public List<FeiShuContentEntity> getContent(String text) {
        List<FeiShuContentEntity> result = new ArrayList<>();
        result.add(new FeiShuContentEntity("text", text));
        return result;
    }
}
