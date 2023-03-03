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

package com.huawei.sermant.backend.webhook.dingding;

import com.alibaba.fastjson.JSONObject;
import com.huawei.sermant.backend.common.conf.CommonConst;
import com.huawei.sermant.backend.entity.DingDingEntity;
import com.huawei.sermant.backend.entity.DingDingMessageType;
import com.huawei.sermant.backend.entity.EventEntity;
import com.huawei.sermant.backend.webhook.WebHookClient;
import com.huawei.sermant.backend.webhook.WebHookConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

/**
 * 钉钉webhook 客户端
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class DingDingHookClient implements WebHookClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DingDingHookClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private WebHookConfig dingDingHookConfig = DingDingHookConfig.getInstance();

    public DingDingHookClient() {
        dingDingHookConfig.setName(CommonConst.DINGDING_WEBHOOK_NAME);
    }

    @Override
    public boolean doNotify(List<EventEntity> eventEntities) {

        DingDingEntity dingDingEntity = new DingDingEntity();
        dingDingEntity.setMsgtype(DingDingMessageType.Text.toString());
        dingDingEntity.setText(new HashMap<String, String>() {{
            put("content", JSONObject.toJSONString(eventEntities));
        }});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        HttpEntity httpEntity = new HttpEntity<>(dingDingEntity, headers);

        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(dingDingHookConfig.getUrl(), httpEntity, JSONObject.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            LOGGER.error(String.format("do notify dingding webhook failed, error message:[%s]", responseEntity.getBody()));
            return false;
        }
        return true;
    }

    @Override
    public WebHookConfig getConfig() {
        return dingDingHookConfig;
    }
}
