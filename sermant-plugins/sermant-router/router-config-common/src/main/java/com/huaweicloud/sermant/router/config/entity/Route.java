/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.config.entity;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Route
 *
 * @author provenceee
 * @since 2021-10-23
 */
public class Route {
    /**
     * Weight
     */
    private Integer weight;

    /**
     * Routing labels
     */
    private Map<String, String> tags;

    /**
     * Routing labels
     */
    @JSONField(name = "tag-inject")
    private Map<String, List<String>> injectTags;

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getWeight() {
        return this.weight;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }

    public Map<String, List<String>> getInjectTags() {
        return injectTags;
    }

    /**
     * Deposit staining markers
     *
     * @param injectTags Stain markers
     */
    public void setInjectTags(Map<String, String> injectTags) {
        if (CollectionUtils.isEmpty(injectTags)) {
            this.injectTags = Collections.emptyMap();
        } else {
            Map<String, List<String>> map = new HashMap<>();
            injectTags.forEach((key, value) -> map.put(key, Collections.singletonList(value)));
            this.injectTags = map;
        }
    }
}