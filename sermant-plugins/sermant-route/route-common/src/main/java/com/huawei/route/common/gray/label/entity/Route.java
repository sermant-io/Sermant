/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.route.common.gray.label.entity;

/**
 * 路由
 *
 * @author provenceee
 * @since 2021/10/23
 */
@SuppressWarnings("checkstyle:RegexpSingleline")
public class Route {
    /**
     * 权重
     */
    private Integer weight;

    /**
     * 路由标签
     */
    private Tags tags;

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getWeight() {
        return this.weight;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Tags getTags() {
        return this.tags;
    }
}
