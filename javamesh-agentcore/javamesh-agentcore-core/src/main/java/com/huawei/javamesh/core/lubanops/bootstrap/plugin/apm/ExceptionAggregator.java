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

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.apm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MultiPrimaryKeyAggregator;

public class ExceptionAggregator extends MultiPrimaryKeyAggregator<ExceptionValue> {

    public static final String AGGREGATE_KEY_TYPE = "type";

    public static final String AGGREGATE_KEY_CAUSE_TYPE = "causeType";

    public static final String AGGREGATE_METRIC_COUNT = "count";

    public static final String AGGREGATE_METRIC_MESSAGE = "message";

    public static final String AGGREGATE_METRIC_STACKTRACE = "stackTrace";

    @Override
    protected Class<ExceptionValue> getValueType() {
        return ExceptionValue.class;
    }

    public void onThrowable(String type, Throwable t) {
        String cause;
        Throwable c = t.getCause();
        if (c == null) {
            cause = "NONE";
        } else {
            cause = c.getClass().getName();
        }
        ExceptionValue exceptionValue = getValue(type, cause);
        exceptionValue.onThrowable(t);
    }

    @Override
    protected int primaryKeyLength() {
        return 2;
    }

    @Override
    protected List<String> primaryKey() {
        return Arrays.asList(AGGREGATE_KEY_TYPE, AGGREGATE_KEY_CAUSE_TYPE);
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }
}
