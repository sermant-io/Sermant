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

package com.huaweicloud.sermant.router.common.request;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class RequestTag {
    private final Map<String, List<String>> tag;

    /**
     * 构造方法
     *
     * @param tag 请求标记 header/attachment
     */
    public RequestTag(Map<String, List<String>> tag) {
        this.tag = CollectionUtils.isEmpty(tag) ? new HashMap<>() : tag;
    }

    public Map<String, List<String>> getTag() {
        return tag;
    }

    /**
     * 增加请求标记
     *
     * @param map 请求标记
     */
    public void addTag(Map<String, List<String>> map) {
        this.tag.putAll(map);
    }
}