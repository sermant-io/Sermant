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

package com.huawei.sermant.core.lubanops.bootstrap.plugin.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.sermant.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.sermant.core.lubanops.bootstrap.collector.api.NonePrimaryKeyAggregator;

/**
 * 默认多版本指标集聚合器
 */
public class DefaultMutiVersionAggregator extends NonePrimaryKeyAggregator {
    private Map<String, String> versionMap = new HashMap<String, String>();

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
        row.putAll(versionMap);
        return row;
    }

    public void setVersion(String jar, String version) {
        this.versionMap.put(jar, version);
    }

    @Override
    public List<MonitorDataRow> getAllStatus() {
        return null;
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    public void clear() {

    }
}
