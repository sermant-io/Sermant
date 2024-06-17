/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale;

import com.alibaba.fastjson.JSONObject;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.mq.grayscale.config.CseMqGrayConfigHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigContextUtils {
    public static JSONObject buildJSONObject(Map<String, Object> map) {
        JSONObject object = new JSONObject();
        object.put("serverGrayEnabled", true);
        object.put("enabled", true);
        List<String> match = new ArrayList<>();
        match.add("test");
        match.add("gray");
        object.put("grayscale.environmentMatch.exact", match);
        object.put("grayscale.trafficMatch.exact", match);
        object.put("base.messageFilter.consumeType", "auto");
        object.put("base.messageFilter.autoCheckDelayTime", 30);
        object.put("base.messageFilter.excludeTags.test", "gray");
        if (map != null) {
            object.putAll(map);
        }
        return object;
    }

    public static void createMqGrayConfig(Map<String, Object> map) {
        CseMqGrayConfigHandler handler = new CseMqGrayConfigHandler();
        String key = "grayscale.mq.config";
        JSONObject object = buildJSONObject(map);
        DynamicConfigEvent event
                = new DynamicConfigEvent(key, "default", object.toString(), DynamicConfigEventType.CREATE);
        handler.handle(event);
    }

    public static void setTrafficTag() {
        List<String> list = new ArrayList<>();
        list.add("gray");
        Map<String, List<String>> tag = new HashMap<>();
        tag.put("test", list);
        TrafficTag trafficTag = new TrafficTag(tag);
        TrafficUtils.setTrafficTag(trafficTag);
    }
}
