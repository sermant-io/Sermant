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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MultiPrimaryKeyAggregator;

/**
 * 默认异常指标集聚合器
 */
public class DefaultExceptionAggregator extends MultiPrimaryKeyAggregator<DefaultExceptionStats> {
    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> rows) {
        return null;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    protected Class<DefaultExceptionStats> getValueType() {
        return DefaultExceptionStats.class;
    }

    @Override
    protected List<String> primaryKey() {
        return Arrays.asList("exceptionType", "causeType");
    }

    public void onThrowable(Throwable t) {
        onThrowable(t, null);
    }

    @Override
    protected int primaryKeyLength() {
        return 2;
    }

    public void onThrowable(Throwable t, String content) {
        if (t == null) {
            return;
        }
        String type = t.getClass().getName();
        String cause;
        Throwable c = t.getCause();
        if (c == null) {
            cause = "NONE";
        } else {
            cause = c.getClass().getName();
        }
        DefaultExceptionStats v = this.getValue(type, cause);
        v.onThrowable(t, content);
    }
}
