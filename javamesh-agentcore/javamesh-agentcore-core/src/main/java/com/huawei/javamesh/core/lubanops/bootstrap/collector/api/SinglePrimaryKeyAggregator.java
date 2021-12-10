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

package com.huawei.javamesh.core.lubanops.bootstrap.collector.api;

import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.exception.ApmRuntimeException;

/**
 * 对于只有单个主键的采集器的基类
 * @param <T>
 * @author frank.yef
 */
public abstract class SinglePrimaryKeyAggregator<T extends StatsBase>
        extends AbstractPrimaryKeyValueAggregator<String, T> {

    /**
     * 默认主键
     */
    protected String defaultKey() {
        return DEFAULT_KEY;
    }

    @Override
    public String getPrimaryKey(Map<String, String> primaryKeyMap) {
        String key = primaryKeyMap.get(primaryKey());
        if (key == null) {
            throw new ApmRuntimeException("Key " + primaryKey() + " is null");
        }
        return key;
    }

    @Override
    protected void setPrimaryKey(MonitorDataRow row, String key) {
        row.put(primaryKey(), key);
    }

    /**
     * 主键
     */
    protected abstract String primaryKey();
}
