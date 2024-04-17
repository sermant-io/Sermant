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
 * WITHOUT WARRANTIES OR C¬ONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.core.utils.tag;

import com.huaweicloud.sermant.core.utils.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TrafficTag
 *
 * @author lilai
 * @since 2023-07-17
 */
public class TrafficTag {
    private final Map<String, List<String>> tag;

    /**
     * constructor
     *
     * @param tag TrafficTag map, http request header/dubbo attachment/MQ header or properties
     */
    public TrafficTag(Map<String, List<String>> tag) {
        this.tag = MapUtils.isEmpty(tag) ? new HashMap<>() : tag;
    }

    public Map<String, List<String>> getTag() {
        return tag;
    }

    /**
     * update TrafficTag
     *
     * @param map TrafficTag map
     */
    public void updateTag(Map<String, List<String>> map) {
        this.tag.putAll(map);
    }
}
