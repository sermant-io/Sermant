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

package com.huaweicloud.sermant.backend.lite.controller;

import com.huaweicloud.sermant.backend.lite.cache.HeartbeatCache;
import com.huaweicloud.sermant.backend.lite.entity.HeartbeatMessage;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 心跳信息Controller
 *
 * @author luanwenfei
 * @since 2022-10-27
 */
@RestController
@RequestMapping("/sermant")
public class HeartBeatInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatInfoController.class);

    @GetMapping("/getPluginsInfo")
    public String getPluginsInfo() {
        return JSONObject.toJSONString(getHeartbeatMessageCache());
    }

    private List<HeartbeatMessage> getHeartbeatMessageCache() {
        Map<String, HeartbeatMessage> heartbeatMessages = HeartbeatCache.getHeartbeatMessageMap();
        return new ArrayList<>(heartbeatMessages.values());
    }
}
