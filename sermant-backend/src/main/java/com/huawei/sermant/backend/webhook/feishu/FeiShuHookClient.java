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

package com.huawei.sermant.backend.webhook.feishu;


import com.alibaba.fastjson.JSONObject;
import com.huawei.sermant.backend.common.conf.CommonConst;
import com.huawei.sermant.backend.entity.EventEntity;
import com.huawei.sermant.backend.entity.FeiShuEntity;
import com.huawei.sermant.backend.entity.FeiShuMessageType;
import com.huawei.sermant.backend.webhook.WebHookClient;
import com.huawei.sermant.backend.webhook.WebHookConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

/**
 * 飞书webhook 客户端
 *
 * @since 2023-03-02
 * @author xuezechao
 */
public class FeiShuHookClient implements WebHookClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeiShuHookClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private WebHookConfig feiShuHookConfig = FeiShuHookConfig.getInstance();

    public FeiShuHookClient() {
        feiShuHookConfig.setName(CommonConst.FEISHU_WEBHOOK_NAME);
    }

    @Override
    public boolean doNotify(List<EventEntity> eventEntities) {

        FeiShuEntity feiShuEntity = new FeiShuEntity();
        feiShuEntity.setMsg_type(FeiShuMessageType.TEXT.toString());
        feiShuEntity.setContent(new HashMap<String, String>() {{
            put(FeiShuMessageType.TEXT.toString(), JSONObject.toJSONString(eventEntities));
        }});

        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(feiShuHookConfig.getUrl(), JSONObject.toJSONString(feiShuEntity), JSONObject.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            LOGGER.error(String.format("do notify feishu webhook failed, error message:[%s]", responseEntity.getBody()));
            return false;
        }
        return true;
    }

    @Override
    public WebHookConfig getConfig() {
        return feiShuHookConfig;
    }
}
