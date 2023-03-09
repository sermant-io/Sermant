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
import com.huaweicloud.sermant.backend.entity.EventInfoEntity;
import com.huaweicloud.sermant.backend.entity.FeiShuEntity;
import com.huaweicloud.sermant.backend.entity.FeiShuMessageType;
import com.huaweicloud.sermant.backend.webhook.WebHookClient;
import com.huaweicloud.sermant.backend.webhook.WebHookConfig;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 飞书webhook 客户端
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class FeiShuHookClient implements WebHookClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeiShuHookClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private WebHookConfig feiShuHookConfig = FeiShuHookConfig.getInstance();

    /**
     * 构造函数
     */
    public FeiShuHookClient() {
        feiShuHookConfig.setName(CommonConst.FEISHU_WEBHOOK_NAME);
    }

    /**
     * webhook推送事件
     *
     * @param eventEntities 事件信息
     * @return
     */
    @Override
    public boolean doNotify(List<EventInfoEntity> eventEntities) {

        FeiShuEntity feiShuEntity = new FeiShuEntity();
        feiShuEntity.setMsgType(FeiShuMessageType.TEXT.getType());
        feiShuEntity.setContent(new HashMap<String, String>() {
            {
                put(FeiShuMessageType.TEXT.toString(), JSONObject.toJSONString(eventEntities));
            }
        });

        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(
                feiShuHookConfig.getUrl(), JSONObject.toJSONString(feiShuEntity), JSONObject.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            LOGGER.error(String.format(Locale.ROOT, "do notify feishu webhook failed, error message:[%s]",
                    responseEntity.getBody()));
            return false;
        }
        return true;
    }

    /**
     * 获取配置
     *
     * @return 配置
     */
    @Override
    public WebHookConfig getConfig() {
        return feiShuHookConfig;
    }
}
