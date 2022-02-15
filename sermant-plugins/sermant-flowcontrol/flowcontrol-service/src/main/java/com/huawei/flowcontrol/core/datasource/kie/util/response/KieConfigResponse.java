/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource.kie.util.response;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * kie配置的响应实体
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class KieConfigResponse {
    @JSONField(name = "total")
    private int total;

    @JSONField(name = "data")
    private List<KieConfigItem> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<KieConfigItem> getData() {
        return data;
    }

    public void setData(List<KieConfigItem> data) {
        this.data = data;
    }
}
