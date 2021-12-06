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
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.SinglePrimaryKeyAggregator;

public class DefaultStatusCodeAggregator extends SinglePrimaryKeyAggregator<DefaultStatusCodeStats> {

    @Override
    public String getName() {
        return "code";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    protected String primaryKey() {
        return "code";
    }

    @Override
    protected Class<DefaultStatusCodeStats> getValueType() {
        return DefaultStatusCodeStats.class;
    }

    public void onStatusCode(String url, int code) {
        if (!isEnable) {
            return;
        }
        DefaultStatusCodeStats value = this.getValue(Integer.toString(code));
        value.getCount().incrementAndGet();
        value.getUrl().set(url);
    }
}
