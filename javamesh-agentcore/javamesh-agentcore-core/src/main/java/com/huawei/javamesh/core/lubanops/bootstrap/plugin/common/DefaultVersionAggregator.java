/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common;

import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.NonePrimaryKeyAggregator;

/**
 * 默认版本指标集聚合器
 */
public class DefaultVersionAggregator extends NonePrimaryKeyAggregator {
    private String version;

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    public MonitorDataRow constructItemRow() {
        MonitorDataRow row = new MonitorDataRow(1);
        if (version != null) {
            row.put("version", version);
        }
        return row;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public List<MonitorDataRow> getAllStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {

    }
}
