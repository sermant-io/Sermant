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

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.url;

import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.SinglePrimaryKeyAggregator;

public class UrlStatusGroupAggregator extends SinglePrimaryKeyAggregator<StatusCodeStats> {

    @Override
    public String getName() {
        return "statuscode";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    protected Class<StatusCodeStats> getValueType() {
        return StatusCodeStats.class;
    }

    public StatusCodeStats onCode(int code) {
        return getValue(code + "");
    }

    @Override
    protected String primaryKey() {
        return "code";
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

}
